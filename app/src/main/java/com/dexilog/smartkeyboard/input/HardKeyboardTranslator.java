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

package com.dexilog.smartkeyboard.input;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.SystemClock;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.dexilog.smartkeyboard.R;

public class HardKeyboardTranslator {
	
	private static final int[] mQwerty = new int[]{
		KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_Y,KeyEvent.KEYCODE_U,KeyEvent.KEYCODE_I,KeyEvent.KEYCODE_O,KeyEvent.KEYCODE_P,
		KeyEvent.KEYCODE_A,KeyEvent.KEYCODE_S,KeyEvent.KEYCODE_D,KeyEvent.KEYCODE_F,KeyEvent.KEYCODE_G,KeyEvent.KEYCODE_H,KeyEvent.KEYCODE_J,KeyEvent.KEYCODE_K,KeyEvent.KEYCODE_L,
		KeyEvent.KEYCODE_Z,KeyEvent.KEYCODE_X,KeyEvent.KEYCODE_C,KeyEvent.KEYCODE_V,KeyEvent.KEYCODE_B,KeyEvent.KEYCODE_N,KeyEvent.KEYCODE_M
	};
	
	private static final int[] mRussian = new int[]{
		KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_Y,KeyEvent.KEYCODE_U,KeyEvent.KEYCODE_I,KeyEvent.KEYCODE_O,KeyEvent.KEYCODE_P,KeyEvent.KEYCODE_LEFT_BRACKET,KeyEvent.KEYCODE_RIGHT_BRACKET,
		KeyEvent.KEYCODE_A,KeyEvent.KEYCODE_S,KeyEvent.KEYCODE_D,KeyEvent.KEYCODE_F,KeyEvent.KEYCODE_G,KeyEvent.KEYCODE_H,KeyEvent.KEYCODE_J,KeyEvent.KEYCODE_K,KeyEvent.KEYCODE_L,KeyEvent.KEYCODE_SEMICOLON,KeyEvent.KEYCODE_APOSTROPHE,KeyEvent.KEYCODE_GRAVE,
		KeyEvent.KEYCODE_Z,KeyEvent.KEYCODE_X,KeyEvent.KEYCODE_C,KeyEvent.KEYCODE_V,KeyEvent.KEYCODE_B,KeyEvent.KEYCODE_N,KeyEvent.KEYCODE_M,KeyEvent.KEYCODE_COMMA,KeyEvent.KEYCODE_PERIOD
	};
	
	private Map<String, Integer> mMultitapMaps = new HashMap<String, Integer>();
	
	private Context mContext;
	private KeyCharacterMap mKeyCharacterMap;
	private char[] mCurMap;
	private Map<Integer, Integer> mQwertyMap = new HashMap<Integer, Integer>();
	private Map<Integer, Integer> mRussianMap = new HashMap<Integer, Integer>();
	private long mLastUpTime = 0;
	private int mLastKeyCode = 0;
	private int mMultiTapCount = 0;
	private boolean mIsMulitap = false;
	private boolean mHebrew = false;
	private boolean mStandardRussian = false;
	private boolean mLastWasUp = true;
	
	private char[] mCurMultitapMap;
	private Map<Integer, Integer> mMultitapIndexMap = new HashMap<Integer, Integer>();

	public HardKeyboardTranslator(Context context) {
		mContext = context;
		// load the character map
		mKeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
		
		final int count = mQwerty.length;
		for (int i=0; i<count; i++) {
			mQwertyMap.put(mQwerty[i], i);
		}
		
		final int russianCount = mRussian.length;
		for (int i=0; i<russianCount; i++) {
			mRussianMap.put(mRussian[i], i);
		}
		mCurMap = null;
		
		// Initialize list of multitap maps
		mMultitapMaps.put("AR", R.string.kbd_multi_ar);
		mMultitapMaps.put("CZ", R.string.kbd_multi_cz);
		mMultitapMaps.put("DA", R.string.kbd_multi_da);
		mMultitapMaps.put("DE", R.string.kbd_multi_de);
		mMultitapMaps.put("ES", R.string.kbd_multi_es);
		mMultitapMaps.put("ET", R.string.kbd_multi_et);
		mMultitapMaps.put("FI", R.string.kbd_multi_fi);
		mMultitapMaps.put("FR", R.string.kbd_multi_fr);
		mMultitapMaps.put("HU", R.string.kbd_multi_hu);
		mMultitapMaps.put("IS", R.string.kbd_multi_is);
		mMultitapMaps.put("IT", R.string.kbd_multi_it);
		mMultitapMaps.put("LT", R.string.kbd_multi_lt);
		mMultitapMaps.put("LV", R.string.kbd_multi_lv);
		mMultitapMaps.put("NO", R.string.kbd_multi_no);
		mMultitapMaps.put("PL", R.string.kbd_multi_pl);
		mMultitapMaps.put("PT", R.string.kbd_multi_pt);
		mMultitapMaps.put("RO", R.string.kbd_multi_ro);
		mMultitapMaps.put("RU", R.string.kbd_multi_ru);
		mMultitapMaps.put("SK", R.string.kbd_multi_sk);
		mMultitapMaps.put("SL", R.string.kbd_multi_sr);
		mMultitapMaps.put("SQ", R.string.kbd_multi_sq);
		mMultitapMaps.put("SR", R.string.kbd_multi_sr);
		mMultitapMaps.put("SV", R.string.kbd_multi_sv);
		mMultitapMaps.put("TR", R.string.kbd_multi_tr);		
		mMultitapMaps.put("UK", R.string.kbd_multi_uk);
	}
	
	public void setLang(String langCode, String lang) {
		mCurMap = null;
		mHebrew = false;
		mStandardRussian = false;
		mCurMultitapMap = null;
		mMultitapIndexMap.clear();
		mMultiTapCount = 0;
		mLastWasUp = true;
		mIsMulitap = false;
		if (langCode.equals("KO")) {
			mCurMap = getCharArray(R.string.kbd_korean);
		} else if (lang.equals("RU_YaShERT")) {
			mCurMap = getCharArray(R.string.kbd_russian);
		} else if (langCode.equals("UK")) {
			mCurMap = getCharArray(R.string.kbd_ukrainian);
		} else if (langCode.equals("HE")) {
			mCurMap = getCharArray(R.string.kbd_hebrew);
			mHebrew = true;
		} else if (langCode.equals("EL")) {
			mCurMap = getCharArray(R.string.kbd_greek);
		} else if (langCode.equals("AR")) {
			mCurMap = getCharArray(R.string.kbd_arabic);
		} else if (langCode.equals("TR")) {
			mCurMap = getCharArray(R.string.kbd_turkish);
		} else if (langCode.equals("RU")) {
			mCurMap = getCharArray(R.string.kbd_russian_standard);
			mStandardRussian = true;
		}
		
		// Load the multitap map
		Integer res = mMultitapMaps.get(langCode);
		if (res != null) {
			loadMultitapMap(res);
		}
	}
	
	private void loadMultitapMap(int res) {
		char[] array = getCharArray(res);
		mCurMultitapMap = array;
		final int len = array.length;
		char curChar = 0;
		for (int i=0; i<len; i++) {
			char c = array[i];
			if (curChar == 0) {
				mMultitapIndexMap.put((int)c, i);
				curChar = c;
			} else if (c == ':') {
				curChar = 0;
			}
		}
	}
	
	private char[] getCharArray(int res) {
		return mContext.getResources().getString(res).toCharArray();
	}
	
	public int translateKey(int keyCode, long meta) {
		boolean altOn = MyMetaKeyKeyListener.getMetaState(meta, MyMetaKeyKeyListener.META_ALT_ON) > 0;
		boolean shiftOn = MyMetaKeyKeyListener.getMetaState(meta, MyMetaKeyKeyListener.META_SHIFT_ON) > 0;

		// If shift is on, let onHandleCharacter handle upper case translation
		if (shiftOn && !altOn) meta = 0;

		int code = 0;
		if (mCurMap != null) {
			Integer index = null;
			if (mStandardRussian) {
				index = mRussianMap.get(keyCode);
			} else {
				index = mQwertyMap.get(keyCode);				
			}
			if (index != null && !altOn) {
				code = mCurMap[index];
			} else if (mHebrew && keyCode == 55) {
				// comma -> tav , shift+comma -> comma
				code = shiftOn ? ',' : '\u05ea';
			} else {
				code = mKeyCharacterMap.get(keyCode, MyMetaKeyKeyListener.getMetaState(meta));
			}
		} else {
			// Default char map
			code = mKeyCharacterMap.get(keyCode, MyMetaKeyKeyListener.getMetaState(meta));
		}
		
		// Check for multitap
		if ((mCurMultitapMap != null) && (keyCode == mLastKeyCode)) {
			if (!mIsMulitap) {
				// Long press detected => enter multitap mode
				if (!mLastWasUp) mIsMulitap = true;
				mMultiTapCount = 1;
			} else if (mLastWasUp){
				// Check time
				final long time = SystemClock.uptimeMillis();
				if (time > mLastUpTime + 600) {
					mIsMulitap = false;
				} else {
					mMultiTapCount++;
				}
			}

			if (mIsMulitap) {
				Integer baseIndex = mMultitapIndexMap.get(code);
				if (baseIndex != null) {
					char c = mCurMultitapMap[baseIndex+mMultiTapCount];
					if (c == ':') {
						mMultiTapCount = 0;
					} else {
						code = c;
					}
				}
			}
		} else {
			mMultiTapCount = 0;
			mIsMulitap = false;
		}

		mLastKeyCode = keyCode;
		mLastWasUp = false;
		return code;
	}
	
	public void keyUp() {
		mLastWasUp = true;
		mLastUpTime = SystemClock.uptimeMillis();
	}
	
	public boolean isMultiTap() {
		return mIsMulitap;
	}
	
}
