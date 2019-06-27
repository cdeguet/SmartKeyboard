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
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
//import android.provider.Contacts;
//import android.provider.ContactsContract.Contacts;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.dexilog.smartkeyboard.settings.PermissionManager;

class ContactsDictionary extends ExpandableDictionary {

	private static final String TAG = "SmartKeyboard";

	private String[] PROJECTION = new String[2];
 /*   private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.DISPLAY_NAME,
    };
*/
	private Uri mContentURI;
    private static final int INDEX_NAME = 1;

    private ContentObserver mObserver;

    private long mLastLoadedContacts;

	public ContactsDictionary(final Context context) {
        super(context);
        
        Log.d("SmartKeyboard", "Loading contacts");

        PermissionManager.get(context).checkContactsPermission(new PermissionManager.PermissionsResultCallback() {
            @Override
            public void onRequestPermissionsResult(boolean allGranted) {
                if (allGranted) {
                    initDictionary(context);
                }
                else {
                    Log.e("SmartKeyboard", "No contacts permission!");

                }
            }
        });
    }

    private void initDictionary(Context context) {
        try {
			Class<?> contactClass = Class.forName("android.provider.ContactsContract$Contacts");
			mContentURI = (Uri)contactClass.getDeclaredField("CONTENT_URI").get(null);
			Class<?> contactColumnsClass = Class.forName("android.provider.ContactsContract$ContactsColumns");
			PROJECTION[0] = android.provider.BaseColumns._ID;
			PROJECTION[1] = (String)contactColumnsClass.getDeclaredField("DISPLAY_NAME").get(null);

		    // Perform a managed query. The Activity will handle closing and requerying the cursor
	        // when needed.
	        ContentResolver cres = context.getContentResolver();

	        cres.registerContentObserver(mContentURI, true, mObserver = new ContentObserver(null) {
	            @Override
	            public void onChange(boolean self) {
	            	setRequiresReload(true);
	            }
	        });

	        loadDictionary();
		} catch (ClassNotFoundException e) {
			Log.i("SmartKeyboard", "No ContactsContract API");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public synchronized void close() {
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }

    @Override
    public void startDictionaryLoadingTaskLocked() {
        long now = SystemClock.uptimeMillis();
        if (mLastLoadedContacts == 0
                || now - mLastLoadedContacts > 30 * 60 * 1000 /* 30 minutes */) {
            super.startDictionaryLoadingTaskLocked();
        }
    }

    @Override
    public void loadDictionaryAsync() {
		long startTime = System.currentTimeMillis();
        Cursor cursor = getContext().getContentResolver()
                .query(mContentURI, PROJECTION, null, null, null);
        if (cursor != null) {
            addWords(cursor);
        }
		Log.i(TAG, "Loaded contact dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
        mLastLoadedContacts = SystemClock.uptimeMillis();
    }

    private void addWords(Cursor cursor) {
        clearDictionary();

        final int maxWordLength = getMaxWordLength();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(INDEX_NAME);

                if (name != null) {
                    int len = name.length();

                    // TODO: Better tokenization for non-Latin writing systems
                    for (int i = 0; i < len; i++) {
                        if (Character.isLetter(name.charAt(i))) {
                            int j;
                            for (j = i + 1; j < len; j++) {
                                char c = name.charAt(j);

                                if (!(c == '-' || c == '\'' ||
                                      Character.isLetter(c))) {
                                    break;
                                }
                            }

                            String word = name.substring(i, j);
                            i = j - 1;

                            // Safeguard against adding really long words. Stack
                            // may overflow due to recursion
                            // Also don't add single letter words, possibly confuses
                            // capitalization of i.
                            final int wordLen = word.length();
                            if (wordLen < maxWordLength && wordLen > 1) {
                                super.addWord(word, 128);
                            }
                        }
                    }
                }

                cursor.moveToNext();
            }
        }
        cursor.close();
    }
}
