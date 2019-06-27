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
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.UserDictionary.Words;
import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposerImpl;
import com.dexilog.smartkeyboard.lang.Korean;

public class UserDictionaryProvider extends ContentProvider {

	private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".userdicprovider";
	public static final Uri CONTENT_URI_WORDS = 
		Uri.parse("content://" + AUTHORITY + "/words");
	// For Korean
	public static final Uri CONTENT_URI_CONVERTED_WORDS = 
		Uri.parse("content://" + AUTHORITY + "/converted_words");
	public static final Uri CONTENT_URI_FREQS = 
		Uri.parse("content://" + AUTHORITY + "/freqs");
	
	public static final String CONTENT_TYPE_WORDS = "vnd.android.cursor.dir/vnd.cdeguet.word";
	public static final String CONTENT_TYPE_FREQS = "vnd.android.cursor.dir/vnd.cdeguet.freq";
	public static final String DATABASE_NAME = "userdic.db";
	public static final String WORDS_TABLE_NAME = "userwords";
	public static final String FREQ_TABLE_NAME = "frequencies";
	public static final String _ID = "_id";
	public static final String WORD = "word";
	public static final String LANG = "lang";
	public static final String COUNT = "count";
	
	private static final int QUERY_WORDS = 0;
    private static final int QUERY_FREQS = 1;
	private static final int QUERY_CONVERTED_WORDS = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

	private DbHelper mHelper;
	private Korean mKorean;
	
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, "words", QUERY_WORDS);
		matcher.addURI(AUTHORITY, "freqs", QUERY_FREQS);
		matcher.addURI(AUTHORITY, "converted_words", QUERY_CONVERTED_WORDS);
		return matcher;
	}
	
	private Korean getKorean() {
		if (mKorean == null) {
			mKorean = new Korean(new WordComposerImpl());
		}
		return mKorean;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		switch (sURIMatcher.match(uri)) {
		case QUERY_WORDS:
		{
			int count = db.delete(WORDS_TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}
		case QUERY_FREQS:
		{
			int count = db.delete(FREQ_TABLE_NAME, selection, selectionArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return count;
		}
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}
	
	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case QUERY_WORDS:
			return CONTENT_TYPE_WORDS;
		case QUERY_FREQS:
			return CONTENT_TYPE_FREQS;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		switch (sURIMatcher.match(uri)) {
		case QUERY_WORDS:
		{
			long rowId = db.insert(WORDS_TABLE_NAME, WORD, values);
			if (rowId > 0) {
				Uri noteUri = ContentUris.withAppendedId(CONTENT_URI_WORDS, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
			return null;
		}
		case QUERY_FREQS:
		{
			long rowId = db.insert(FREQ_TABLE_NAME, WORD, values);
			if (rowId > 0) {
				Uri noteUri = ContentUris.withAppendedId(CONTENT_URI_FREQS, rowId);
				getContext().getContentResolver().notifyChange(noteUri, null);
				return noteUri;
			}
			return null;
		}
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		mHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
	    SQLiteDatabase db = mHelper.getReadableDatabase();

		switch (sURIMatcher.match(uri)) {
		case QUERY_WORDS:
		{
		    qb.setTables(WORDS_TABLE_NAME); 
		    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			return c;
		}
		case QUERY_FREQS:
		{
		    qb.setTables(FREQ_TABLE_NAME); 
		    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			return c;
		}	
		case QUERY_CONVERTED_WORDS:
		{
		    qb.setTables(WORDS_TABLE_NAME); 
		    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			return new ConvertedCursor(c);
		}
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mHelper.getWritableDatabase();
		switch (sURIMatcher.match(uri)) {
		case QUERY_WORDS:
		{
			int count = db.update(WORDS_TABLE_NAME, values, selection, selectionArgs);
	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
		}
	    case QUERY_FREQS:
		{
			int count = db.update(FREQ_TABLE_NAME, values, selection, selectionArgs);
	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
		}
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}


	private static class DbHelper extends SQLiteOpenHelper {

		private Context mContext;
		
		public DbHelper(Context context) {
			super(context, DATABASE_NAME, null, 2);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + WORDS_TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ WORD + " TEXT,"
					+ LANG + " LANG"
					+ ");");
			createFreqTable(db);
			
			// Import words from the user dictionary (with English locale)
			Cursor cursor = mContext.getContentResolver().query(Words.CONTENT_URI, 
					new String[] { Words.WORD }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				ContentValues values = new ContentValues();
				do {
					final String word = cursor.getString(0);
					Log.d("SmartKeyboard", "Import " + word + " from user dictionary");
					values.clear();
					values.put(UserDictionaryProvider.WORD, word);
					values.put(UserDictionaryProvider.LANG, "EN");
					db.insert(WORDS_TABLE_NAME, WORD, values);
				} while (cursor.moveToNext());
				cursor.close();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("SmartKeyboard", "Upgrading DB from " + Integer.toString(oldVersion) +
					" to " + Integer.toString(newVersion));
			if (oldVersion < 2) {
				db.execSQL("DROP TABLE " + FREQ_TABLE_NAME );
				createFreqTable(db);
			}
		}
		
		private void createFreqTable(SQLiteDatabase db) {
			// Starting from v2
			db.execSQL("CREATE TABLE " + FREQ_TABLE_NAME + " ("
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ WORD + " TEXT,"
					+ LANG + " LANG,"
					+ COUNT + " INTEGER"
					+ ");");
			db.execSQL("CREATE INDEX " + FREQ_TABLE_NAME + "_IDX ON " +
					FREQ_TABLE_NAME + " (WORD, LANG)");
		}

	}
	
	
	private class ConvertedCursor extends CursorWrapper {

		private Korean mKorean;
		private StringBuilder mBuilder = new StringBuilder();
		
		public ConvertedCursor(Cursor cursor) {
			super(cursor);
			mKorean = getKorean();
		}
		
		@Override
		public String getString(int columnIndex) {
			String value = super.getString(columnIndex);
			mBuilder.setLength(0);
			mKorean.convert(value, mBuilder);
			return mBuilder.toString();
		}
		
	}

}
