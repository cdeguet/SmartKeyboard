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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class AutoTextProvider extends ContentProvider {

	public static final Uri CONTENT_URI = 
		Uri.parse("content://" + BuildConfig.APPLICATION_ID + ".autotextprovider");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.cdeguet.autotext";
	public static final String DATABASE_NAME = "autotext.db";
	public static final String TABLE_NAME = "autotext";
	public static final String _ID = "_id";
	public static final String KEY = "key";
	public static final String VALUE = "value";

	private DbHelper mHelper;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count = db.delete(TABLE_NAME, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return CONTENT_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, KEY, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(AutoTextProvider.CONTENT_URI, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		return null;
	}

	@Override
	public boolean onCreate() {
		mHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		//Log.d("AutoTextProvider", uri.toString());
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    qb.setTables(TABLE_NAME);
	     
	    SQLiteDatabase db = mHelper.getReadableDatabase();
	    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int count = db.update(TABLE_NAME, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}


	private static class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ KEY + " TEXT,"
					+ VALUE + " TEXT"
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
		}

	}

}
