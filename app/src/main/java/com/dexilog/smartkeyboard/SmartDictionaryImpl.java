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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.dexilog.smartkeyboard.suggest.SmartDictionary;

public class SmartDictionaryImpl extends ExpandableDictionary implements SmartDictionary {

	private static final boolean DEBUG = false;
	private static final String TAG = "SmartKeyboard";

	String mLang;
	boolean mActive;
	ExpandableDictionary mFrequencies;
	private Handler mHandler;
    private boolean mRequiresReload;
    private boolean mUpdatingDictionary;
    // Use this lock before touching mUpdatingDictionary & mRequiresDownload
    private Object mUpdatingLock = new Object();
    
	public SmartDictionaryImpl(Context context, String lang) {
		super(context);
		mLang = lang;
		mActive = !lang.equals("ZH");
		
		// Start child thread
		new LooperThread().start();
		
        loadDictionary();
	}

	
    @Override
    public void loadDictionaryAsync() {
		// Fill the frequency cache
		long startTime = System.currentTimeMillis();
		
		final int maxWordLen = getMaxWordLength();
		Cursor cursor = getContext().getContentResolver().query(UserDictionaryProvider.CONTENT_URI_FREQS,
				new String[] { "word", "count"}, "lang=?", new String[] { mLang }, null);
		if (cursor != null) {
			CharArrayBuffer wordBuf = new CharArrayBuffer(40);
			if (cursor.moveToFirst()) {
				do {
					cursor.copyStringToBuffer(0, wordBuf);
					int count = cursor.getInt(1);
					//if (DEBUG) Log.d(TAG, "Add word " + word + " " + mLang + " " + Integer.toString(count));
					// Avoid stack overflow
					if (wordBuf.sizeCopied < maxWordLen) {
						addCharArray(wordBuf.data, wordBuf.sizeCopied, count);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		Log.i(TAG, "Loaded smart dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
	}

	@Override
	public void increaseWordCount(String word) {
        synchronized (mUpdatingLock) {
            // If we need to update, start off a background task
            if (mRequiresReload) startDictionaryLoadingTaskLocked();
            // Currently updating, don't return any results.
            if (mUpdatingDictionary) return;
        }
		
        if (!mActive) return;
        
		if (mHandler != null) {
			int count = increaseWordFrequency(word);
			if (count == -1) {
				addWord(word, 1);
			}

			// post a message to update the DB
			Message message = mHandler.obtainMessage();
			message.arg1 = count == -1 ? 0 : count;
			message.obj = new Key(mLang, word);
			mHandler.sendMessage(message);
		}
	}

	@Override
	public int getWordCount(CharSequence word) {
        synchronized (mUpdatingLock) {
            // If we need to update, start off a background task
            if (mRequiresReload) startDictionaryLoadingTaskLocked();
            // Currently updating, don't return any results.
            if (mUpdatingDictionary) return 0;
        }
		
        if (!mActive) return 0;
        
		int count = getWordFrequency(word);
		return count != -1 ? count : 0;
	}


	static class Key {
		public String mLang;
		public String mWord;
		
		public Key(String lang, String word) {
			mLang = lang;
			mWord = word;
		}
 	}
	

	class LooperThread extends Thread {
		ContentResolver mContentResolver = getContext().getContentResolver();
		ContentValues mValues = new ContentValues();

		public void run() {
			Looper.prepare();

			mHandler = new Handler() {
				public void handleMessage(Message msg) {
					Key key = (Key)msg.obj;
					int count = msg.arg1;
					if (DEBUG) Log.d(TAG, "handleMessage " + key.mWord + " " + Integer.toString(count));
					// Update the DB
					mValues.clear();
					mValues.put(UserDictionaryProvider.WORD, key.mWord);
					mValues.put(UserDictionaryProvider.LANG, key.mLang);
					mValues.put(UserDictionaryProvider.COUNT, count + 1);
					if (count == 0) {
						mContentResolver.insert(UserDictionaryProvider.CONTENT_URI_FREQS, mValues);
					} else {
						mContentResolver.update(UserDictionaryProvider.CONTENT_URI_FREQS, mValues,
								"WORD=? AND LANG=?", new String[] { key.mWord, key.mLang });
					}
				}
			};

			Looper.loop();
		}
	}
}
