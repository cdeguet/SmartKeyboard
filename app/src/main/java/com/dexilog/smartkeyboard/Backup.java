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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.dexilog.smartkeyboard.settings.PermissionManager;

class Backup {

    public interface BackupCallback {
        void onSuccess();
        void onFailed();
    }

    public static final String BACKUP_DIR_PRO = "smartkeyboardpro";
    private static final String BACKUP_FILE = "backup.zip";
    private static final String TAG = "SmartKeyboard";
    static final int BUFFER_SIZE = 2048;
    private final Context context;
    byte[] mBuffer = new byte[BUFFER_SIZE];

    private final String[] mFiles = {
            "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/autotext.db",
            "/data/data/" + BuildConfig.APPLICATION_ID + "/databases/userdic.db",
            // Hack for Samsung Galaxy S
            "/dbdata/databases/" + BuildConfig.APPLICATION_ID + "/shared_prefs/" + BuildConfig.APPLICATION_ID + "_preferences.xml"
    };

    private File mBackupDir;

    public Backup(Context context) {
        this.context = context;
        mBackupDir = new File(Environment.getExternalStorageDirectory(), BACKUP_DIR_PRO);
        mBackupDir.mkdir();
    }

    public void backup(final BackupCallback callback) {
        PermissionManager.get(context).checkWriteStoragePermission(
                new PermissionManager.PermissionsResultCallback() {
                    @Override
                    public void onRequestPermissionsResult(boolean allGranted) {
                        if (allGranted)
                            doBackup(callback);
                        else
                            callback.onFailed();
                    }
                }
        );
    }

    private void doBackup(BackupCallback callback) {
        // Create zip file
        File zipFile = new File(mBackupDir, BACKUP_FILE);
        zipFile.delete();
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
            for (int i = 0; i < mFiles.length; i++) {
                String filePath = mFiles[i];
                try {
                    backupFile(zos, filePath);
                } catch (FileNotFoundException e) {
                    // Hack for Galaxy S
                    Log.i(TAG, "Could not find file. Trying in other directory...");
                    backupFile(zos, filePath.replace("/dbdata/databases", "/data/data"));
                }
            }
            zos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onFailed();
        } catch (IOException e) {
            e.printStackTrace();
            callback.onFailed();
        }
        callback.onSuccess();
    }

    private void backupFile(ZipOutputStream zos, final String filePath) throws IOException {
        Log.i(TAG, "Try to backup file " + filePath);
        final File file = new File(filePath);
        // Zip one file
        int bytesIn = 0;
        FileInputStream fis = new FileInputStream(file);
        ZipEntry entry = new ZipEntry(file.getPath());
        zos.putNextEntry(entry);
        while ((bytesIn = fis.read(mBuffer)) != -1) {
            zos.write(mBuffer, 0, bytesIn);
        }
        fis.close();
    }

    public void restore(final BackupCallback callback) {
        PermissionManager.get(context).checkReadStoragePermission(new PermissionManager.PermissionsResultCallback() {
            @Override
            public void onRequestPermissionsResult(boolean allGranted) {
                if (allGranted)
                    doRestore(callback);
                else
                    callback.onFailed();
            }
        });
    }

    private void doRestore(BackupCallback callback) {
        File zipFile = new File(mBackupDir, BACKUP_FILE);
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new
                    FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis, BUFFER_SIZE));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Log.i(TAG, "Restore file " + entry);
                int count;

                String strEntry = entry.getName();
                // If was saved from trial version, change the target directory
                if (BuildConfig.PRO) {
                    strEntry = strEntry.replace("smartkeyboardtrial", "smartkeyboardpro");
                } else {
                    strEntry = strEntry.replace("smartkeyboardpro", "smartkeyboardtrial");
                }
                File entryFile = new File(strEntry);
                File entryDir = new File(entryFile.getParent());
                if (!entryDir.exists()) {
                    entryDir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(entryFile);
                dest = new BufferedOutputStream(fos, BUFFER_SIZE);
                while ((count = zis.read(mBuffer, 0, BUFFER_SIZE)) != -1) {
                    // Sanity check
                    if (count == 0) {
                        Log.e(TAG, "File is corrupted! Aborting");
                        callback.onFailed();
                    }
                    dest.write(mBuffer, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFailed();
        }
        callback.onSuccess();
    }

}
