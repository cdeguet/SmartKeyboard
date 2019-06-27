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

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

import com.dexilog.smartkeyboard.suggest.Dictionary;

public class AutoTextDictionary {

	private static final String TAG = "SmartKeyboard";
	static final int FREQUENCY = 4096 * 4096;
	private Context mContext;
	private ContentObserver mObserver;
	private Map<String, Vector<String>> mAutoTextMap;
	private boolean mTypeWordValid;
	
	public AutoTextDictionary(Context context) {
		mContext = context;
		mAutoTextMap = new HashMap<String, Vector<String>>();
		mObserver = new DicObserver();
		context.getContentResolver().registerContentObserver(AutoTextProvider.CONTENT_URI, true, mObserver);
		reloadDictionary();
	}
	
	private void reloadDictionary() {
		Log.d(TAG, "Loading autotext dictionary...");
		mAutoTextMap.clear();
		Cursor cursor = mContext.getContentResolver().query(AutoTextProvider.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			final int keyIndex = cursor.getColumnIndex(AutoTextProvider.KEY);
			final int valueIndex = cursor.getColumnIndex(AutoTextProvider.VALUE);
			if (cursor.moveToFirst()) {
				do {
					final String key = cursor.getString(keyIndex).toLowerCase();
					final String value = cursor.getString(valueIndex);
					Vector<String> vector = mAutoTextMap.get(key);
					if (vector == null) {
						vector = new Vector<String>();
						mAutoTextMap.put(key, vector);
					}
					vector.add(value);
					//Log.d("SmartKeyboard", "Add autotext " + key + "->" + value);
				} while (cursor.moveToNext());
			}
			cursor.close();
		} else {
			Log.e(TAG, "Failed to load dictionary!");
		}
	}
	
	public boolean getWords(String word, Dictionary.WordCallback callback) {
		final Vector<String> vector = mAutoTextMap.get(word);
		boolean found = false;
		mTypeWordValid = false;
		final int finalFreq = FREQUENCY * word.length();
		if (vector != null) {
			found = true;
			//Log.d("SmartKeyboard", "Found autotext: " + value);
			for (int i=0; i<vector.size(); i++) {
				final String value = vector.get(i);
				// Check for macros
				String newValue = applyMacros(value);
				callback.addWord(newValue.toCharArray(), 0, newValue.length(), finalFreq);
				if (word.equals(value)) {
					mTypeWordValid = true;
				}
			}
		}
		return found;
	}

	public boolean isTypedWordValid() {
		return mTypeWordValid;
	}
	
	private String applyMacros(String text) {
		StringBuilder sb = new StringBuilder();
		final int len = text.length();
		for (int i=0; i<len; i++) {
			char c = text.charAt(i);
			if (c == '%' && i<len-1) {
				i++;
				char macro = text.charAt(i);
				evaluateMacro(macro, sb);
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	private void evaluateMacro(char macro, StringBuilder sb) {
		switch (macro) {
		case '%':
			sb.append('%');
			break;
		case 't':
			// short time
		    if (DateFormat.is24HourFormat(mContext)) {
		    	sb.append(DateFormat.format("k:mm", new Date()));
		    } else {
		    	sb.append(DateFormat.format("h:mm AA", new Date()));
		    }
		    break;
		case 'T':
			// long time
		    if (DateFormat.is24HourFormat(mContext)) {
		    	sb.append(DateFormat.format("k:mm:ss", new Date()));
		    } else {
		    	sb.append(DateFormat.format("h:mm:ss AA", new Date()));
		    }
		    break;
		case 'd':
			// short date
			SimpleDateFormat dateFormat = (SimpleDateFormat)DateFormat.getDateFormat(mContext);
			// Make sure the year is in 4 digits
			final String pattern = dateFormat.toPattern();
			if (!pattern.contains("yyyy")) {
				dateFormat.applyPattern(pattern.replace("yy", "yyyy"));
			}
			sb.append(dateFormat.format(new Date()));
			break;
		case 'D':
			// long date
			Date now = new Date();
			sb.append(DateFormat.format("E, ", now));
			sb.append(DateFormat.getMediumDateFormat(mContext).format(now));
			break;
		}
	}
	
	private class DicObserver extends ContentObserver {
        public DicObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            reloadDictionary();
        }
    }

}
