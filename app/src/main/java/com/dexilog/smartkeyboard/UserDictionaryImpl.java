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
import android.database.ContentObserver;
import android.database.Cursor;
import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.suggest.UserDictionary;

public class UserDictionaryImpl extends ExpandableDictionary implements UserDictionary {
    
	private static final String TAG = "SmartKeyboard";
	
    private static final String[] PROJECTION = {
        UserDictionaryProvider._ID,
        UserDictionaryProvider.WORD
    };
    
    private static final int INDEX_WORD = 1;
    
    private ContentObserver mObserver;
    
    private boolean mRequiresReload;
    private String mLang;
    
    public UserDictionaryImpl(Context context, String lang) {
        super(context);
        mLang = lang;
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        ContentResolver cres = context.getContentResolver();
        
        cres.registerContentObserver(UserDictionaryProvider.CONTENT_URI_WORDS, true, mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean self) {
                mRequiresReload = true;
            }
        });

        loadDictionary();
    }
    
    @Override
    public synchronized void close() {
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        super.close();
    }
    
    @Override
    public void loadDictionaryAsync() {
		long startTime = System.currentTimeMillis();
        Cursor cursor = getContext().getContentResolver()
                .query(UserDictionaryProvider.CONTENT_URI_WORDS, PROJECTION, "LANG=?", 
                        new String[] { mLang }, null);
        if (cursor != null) {
        	addWords(cursor);
        } else {
        	Log.e(TAG, "Cannot read user dictionary");
        }
		Log.i(TAG, "Loaded user dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
        mRequiresReload = false;
    }

    /**
     * Adds a word to the dictionary and makes it persistent.
     * @param word the word to add. If the word is capitalized, then the dictionary will
     * recognize it as a capitalized word when searched.
     * @param frequency the frequency of occurrence of the word. A frequency of 255 is considered
     * the highest.
     * @TODO use a higher or float range for frequency
     */
    @Override
    public synchronized void addWord(String word, int frequency) {
		//Log.d("KBD", "addWord (User)" + word);
    	
        if (mRequiresReload) loadDictionary();
        // Safeguard against adding long words. Can cause stack overflow.
        if (word.length() >= getMaxWordLength()) return;

        super.addWord(word, frequency);

		ContentValues values = new ContentValues();
		values.put(UserDictionaryProvider.WORD, word);
		values.put(UserDictionaryProvider.LANG, mLang);
		getContext().getContentResolver().insert(UserDictionaryProvider.CONTENT_URI_WORDS, values);
        
        // In case the above does a synchronous callback of the change observer
        mRequiresReload = false;
    }

    @Override
    public synchronized void getWords(final WordComposer codes, final WordCallback callback, boolean modeT9,
                                      int[] nextLettersFrequencies) {
    	//Log.d("KBD", "UserDic getWords");
        if (mRequiresReload) loadDictionary();
        super.getWords(codes, callback, modeT9, nextLettersFrequencies);
    }

    @Override
    public synchronized boolean isValidWord(CharSequence word) {
        if (mRequiresReload) loadDictionary();
        return super.isValidWord(word);
    }

    private void addWords(Cursor cursor) {
        clearDictionary();

        final int maxWordLength = getMaxWordLength();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String word = cursor.getString(INDEX_WORD);
                int frequency = 128;
            	//Log.d("KBD", "addWords: " + word);
                // Safeguard against adding really long words. Stack may overflow due
                // to recursion
                if (word.length() < maxWordLength) {
                    super.addWord(word, frequency);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }
}
