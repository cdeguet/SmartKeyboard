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

import java.io.IOException;

import jp.co.omronsoft.openwnn.WnnWord;
import jp.co.omronsoft.openwnn.JAJP.OpenWnnEngineJAJP;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;

import com.dexilog.smartkeyboard.input.WordComposer;
import com.dexilog.smartkeyboard.suggest.Dictionary;

@SuppressWarnings("serial")
public class Japanese extends Dictionary {

    private static final String TAG = "SmartKeyboard";

	private OpenWnnEngineJAJP mDictionary;

	public Japanese(Context context) throws NameNotFoundException, IOException {
        long startTime = System.currentTimeMillis();

		final String pkgName = "net.cdeguet.smartkeyboardpro.jp";
		AssetFileDescriptor fd = null;
		Resources res = context.getPackageManager().getResourcesForApplication(pkgName);
		fd = res.getAssets().openFd("jp_dic.mp3");

		mDictionary = new OpenWnnEngineJAJP(fd);
		mDictionary.init();
		mDictionary.setKeyboardType(OpenWnnEngineJAJP.KEYBOARD_QWERTY);
		fd.close();
        Log.i(TAG, "Loaded dictionary in " + (System.currentTimeMillis() - startTime) + "msec");
	}


	@Override
	public void getWords(WordComposer composer, WordCallback callback,
						 boolean modeT9, int[] nextLettersFrequencies) {

		OpenWnnEngineJAJP dict = mDictionary;
		mDictionary.predict(composer, -1);
		WnnWord result;
		int i = 0;
		String previous = null;
		while ((result = dict.getNextCandidate()) != null && i<100) {
			if (previous == null || !previous.equals(result.candidate)) {
				callback.addWord(result.candidate.toCharArray(), 0, 
						result.candidate.length(), Math.max(result.frequency, 1));
			}
			i++;
			previous = result.candidate;
		}		
	}

	@Override
	public boolean isValidWord(CharSequence word) {
		// TODO Auto-generated method stub
		return false;
	}

}
