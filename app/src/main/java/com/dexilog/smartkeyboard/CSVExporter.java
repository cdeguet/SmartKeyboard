/*
 * Copyright (C) 2010-2017 Cyril Deguet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dexilog.smartkeyboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import com.dexilog.smartkeyboard.settings.PermissionManager;

import static com.dexilog.smartkeyboard.Backup.BACKUP_DIR_PRO;


public class CSVExporter {

    public interface ExporterCallback {
        void onSuccess();

        void onFailed();
    }

    Context context;
    private File backupDir;

    private static final String[] PROJECTION_USERDIC = {
            UserDictionaryProvider.WORD,
            UserDictionaryProvider.LANG
    };

    private static final String[] PROJECTION_AUTOTEXT = {
            AutoTextProvider.KEY,
            AutoTextProvider.VALUE
    };

    public CSVExporter(Context context) {
        this.context = context;
        backupDir = new File(Environment.getExternalStorageDirectory(), BACKUP_DIR_PRO);
        backupDir.mkdir();
    }

    public void exportUserDic(final String path, final ExporterCallback callback) {
        PermissionManager.get(context).checkWriteStoragePermission(
                new PermissionManager.PermissionsResultCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted) {
                        if (allGranted)
                            exportData(path, callback, new String[]{"word", "lang"},
                                    UserDictionaryProvider.CONTENT_URI_WORDS, PROJECTION_USERDIC);
                        else
                            callback.onFailed();
                    }
                });
    }

    public void exportAutotext(final String path, final ExporterCallback callback) {
        PermissionManager.get(context).checkWriteStoragePermission(
                new PermissionManager.PermissionsResultCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted) {
                        if (allGranted)
                            exportData(path, callback, new String[]{"key", "value"},
                                    AutoTextProvider.CONTENT_URI, PROJECTION_AUTOTEXT);
                        else
                            callback.onFailed();
                    }
                });
    }

    private void exportData(String path, ExporterCallback callback, String[] header, Uri contentURI,
                            String[] projection) {
        try {
            // Open the destination file
            File userDicFile = new File(backupDir, path);
            ICsvListWriter writer = new CsvListWriter(new FileWriter(userDicFile), CsvPreference.STANDARD_PREFERENCE);

            // Write the header
            writer.writeHeader(header);

            // Read the whole dictionary
            List<String> data = new LinkedList<String>();
            Cursor cursor = context.getContentResolver().query(contentURI, projection, null, null, null);
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    data.clear();
                    data.add(cursor.getString(0)); // word
                    data.add(cursor.getString(1)); // lang
                    writer.write(data);
                    cursor.moveToNext();
                }
            }
            writer.close();
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed();
        }
        callback.onSuccess();
    }

    public void importUserDic(final String path, final ExporterCallback callback) {
        PermissionManager.get(context).checkReadStoragePermission(
                new PermissionManager.PermissionsResultCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted) {
                        if (allGranted)
                            doImportUserDic(path, callback);
                        else
                            callback.onFailed();
                    }
                });
    }

    private void doImportUserDic(String path, ExporterCallback callback) {
        try {
            // Open the source file
            ICsvListReader reader = getCSVReader(path);

            // Delete the user dictionary
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(UserDictionaryProvider.CONTENT_URI_WORDS, null, null);
            List<String> data;
            ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
            // Write each entry to the user dictionary
            while (true) {
                data = reader.read();
                if (data == null) break;
                ContentValues values = new ContentValues();
                values.put(UserDictionaryProvider.WORD, data.get(0));
                values.put(UserDictionaryProvider.LANG, data.get(1));
                valuesList.add(values);
            }
            reader.close();
            // Bulk insert is faster!
            ContentValues valuesArray[] = new ContentValues[valuesList.size()];
            valuesArray = valuesList.toArray(valuesArray);
            resolver.bulkInsert(UserDictionaryProvider.CONTENT_URI_WORDS, valuesArray);
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed();
        }
        callback.onSuccess();
    }

    private ICsvListReader getCSVReader(String path) throws IOException {
        File userDicFile = new File(backupDir, path);
        ICsvListReader reader = new CsvListReader(new FileReader(userDicFile), CsvPreference.STANDARD_PREFERENCE);
        // Read the header
        reader.getCSVHeader(true);
        return reader;
    }

    public void importAutotext(final String path, final ExporterCallback callback) {
        PermissionManager.get(context).checkReadStoragePermission(
                new PermissionManager.PermissionsResultCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted) {
                        if (allGranted)
                            doImportAutoText(path, callback);
                        else
                            callback.onFailed();
                    }
                });
    }

    private void doImportAutoText(String path, ExporterCallback callback) {
        try {
            // Open the source file
            ICsvListReader reader = getCSVReader(path);

            // Delete the autotext dictionary
            ContentResolver resolver = context.getContentResolver();
            resolver.delete(AutoTextProvider.CONTENT_URI, null, null);
            List<String> data;
            ContentValues values = new ContentValues();
            // Write each entry to the autotext dictionary
            while (true) {
                data = reader.read();
                if (data == null) break;
                values.clear();
                values.put(AutoTextProvider.KEY, data.get(0));
                values.put(AutoTextProvider.VALUE, data.get(1));
                resolver.insert(AutoTextProvider.CONTENT_URI, values);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed();
        }
        callback.onSuccess();
    }

}
