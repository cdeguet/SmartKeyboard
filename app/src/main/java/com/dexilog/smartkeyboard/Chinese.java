/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.FileDescriptor;
import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.suggest.Dictionary;

/**
 * This class is used to separate the input method kernel in an individual
 * service so that both IME and IME-syncer can use it.
 */
public class Chinese extends Dictionary {
	
	native static boolean nativeImOpenDecoderFd(FileDescriptor fd,
			long startOffset, long length, byte fn_usr_dict[]);

	native static String nativeImGetChoice(int choiceId);

	native static void nativeImResetSearch();

	native static int nativeImSearch(byte pyBuf[], int pyLen);
	
	/* UNUSED 

	native static boolean nativeImOpenDecoder(byte fn_sys_dict[],
			byte fn_usr_dict[]);

	native static void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);

	native static boolean nativeImCloseDecoder();


	native static int nativeImDelSearch(int pos, boolean is_pos_in_splid,
			boolean clear_fixed_this_step);


	native static int nativeImAddLetter(byte ch);

	native static String nativeImGetPyStr(boolean decoded);

	native static int nativeImGetPyStrLen(boolean decoded);

	native static int[] nativeImGetSplStart();

	native static int nativeImChoose(int choiceId);

	native static int nativeImCancelLastChoice();

	native static int nativeImGetFixedLen();

	native static boolean nativeImCancelInput();

	native static boolean nativeImFlushCache();

	native static int nativeImGetPredictsNum(String fixedStr);

	native static String nativeImGetPredictItem(int predictNo);
	*/

	byte[] mPyBuf = new byte[28];
	
    private static final String TAG = "SmartKeyboard";

	static {
		try {
			System.loadLibrary("smartkbdpinyin");
		} catch (UnsatisfiedLinkError ule) {
			Log.e("TAG",
			"WARNING: Could not load pinyin library");
		}
	}


	public void initPinyinEngine(Context context) throws NameNotFoundException, IOException {
        long startTime = System.currentTimeMillis();

		// Here is how we open a built-in dictionary for access through
		// a file descriptor...
		Resources res;
		res = context.getPackageManager().getResourcesForApplication("net.cdeguet.smartkeyboardpro.zh");
		AssetFileDescriptor afd = res.getAssets().openFd("zh_dic.mp3");
		nativeImOpenDecoderFd(afd.getFileDescriptor(), afd
				.getStartOffset(), afd.getLength(), null);
		afd.close();
        Log.i(TAG, "Loaded dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
	}

	@Override
	public void getWords(WordComposer composer, WordCallback callback,
						 boolean modeT9, int[] nextLettersFrequencies) {
		CharSequence word = composer.getTypedWord();
		for (int i = 0; i < word.length(); i++)
			mPyBuf[i] = (byte) word.charAt(i);
		mPyBuf[composer.size()] = 0;
		nativeImResetSearch();
		int count = nativeImSearch(mPyBuf, word.length());
		
		for (int i=0; i<count; i++) {
			String candidate = nativeImGetChoice(i);
			callback.addWord(candidate.toCharArray(), 0, 
					candidate.length(), count+1-i);
		}

	}

	@Override
	public boolean isValidWord(CharSequence word) {
		// TODO Auto-generated method stub
		return false;
	}


}
