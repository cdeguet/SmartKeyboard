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

package com.dexilog.smartkeyboard.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.dexilog.openskin.OpenSkin;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;

import com.dexilog.smartkeyboard.R;

public class SkinLoader {
	
	static final String TAG = "SmartKeyboard";
	static final String SKIN_CACHE = "curskin.zip";
	static final int BUFFER_SIZE = 2048;
	final boolean DEBUG = false;
	Context mContext;
	String mCurSkin = "";
	int mOrientation; // 0 = portrait, 1 = landscape
	SkinInfo[] mSkinInfo = {null, null};
	public SkinInfo mPopupSkin;

	public SkinLoader(Context context, int orientation) {
		mContext = context;
		setOrientation(orientation);
		// Load the popup skin
		mPopupSkin = loadBuiltinSkin(R.style.popup);
	}
	
	public SkinInfo getCurrentSkin() {
		SkinInfo skin = mSkinInfo[mOrientation];
		if (skin == null) {
			// new orientation, reload skin
			skin = loadSkinImpl(mCurSkin);
			mSkinInfo[mOrientation] = skin;
		}
		return skin;
	}
	
	public void setOrientation(int orientation) {
		mOrientation = orientation == Configuration.ORIENTATION_PORTRAIT ? 0 : 1;
	}
	
	public void loadSkin(String skinName) {
		if (skinName.equals(mCurSkin)) {
			// Don't reload the same skin...
			return;
		}
		mSkinInfo[0] = null;
		mSkinInfo[1] = null;
		mSkinInfo[mOrientation] = loadSkinImpl(skinName);
		mCurSkin = skinName;
	}
	
	static public void cacheOpenSkin(Context context, String path) {
		// Copy to the internal storage for more performance
		try {
			FileOutputStream fos = context.openFileOutput(SKIN_CACHE, Context.MODE_PRIVATE);
			FileInputStream fis = new FileInputStream(path);
			byte buffer[] = new byte[BUFFER_SIZE];
			// Copy
			int bytesIn = 0;
			while ((bytesIn = fis.read(buffer)) != -1) { 
				fos.write(buffer, 0, bytesIn); 
			}
			fos.close();
			Log.d(TAG, "Skin " + path + " copied to internal storage");
		} catch (Exception e) {
			Log.e(TAG, "Failed to copy " + path + " to internal storage");
			e.printStackTrace();
		}
	}
	
	
	private SkinInfo loadSkinImpl(String skinName) {
		SkinInfo skin;
		if (skinName.startsWith("bk:")) {
			// Better Keyboard skin
			skin = loadBKSkin(skinName.substring(3));
		} else	if (skinName.startsWith("os:")) {
			// Open skin
			skin = loadOpenSkin(skinName.substring(3));
		} else {
			// Built-in style
			int style = getStyle(skinName);
			skin = loadBuiltinSkin(style);
		}
		skin.popupSkin = mPopupSkin;
		final Resources res = mContext.getResources();
		if (skin.suggestBackground == null) {
			skin.suggestBackground = res.getDrawable(R.drawable.keyboard_suggest_strip);
		}
		if (skin.suggestDivider == null) {
			skin.suggestDivider = res.getDrawable(R.drawable.keyboard_suggest_strip_divider);
		}
		return skin;
	}

	private int getStyle(String skin) {
		if (skin.equals("Android")) {
			return R.style.Android;
		} else if (skin.equals("Galaxy")) {
			return R.style.Galaxy;
		} else if (skin.equals("Gray")) {
			return R.style.Gray;
		} else if (skin.equals("White")) {
			return R.style.White;
		} else if (skin.equals("Black")) {
			return R.style.Black;
		} else if (skin.equals("HTC")) {
			return R.style.HTC;
		} else if (skin.equals("Gingerbread")) {
			return R.style.Gingerbread;
		} else {
			return R.style.iPhone;
		}
	}
	
	private SkinInfo loadBuiltinSkin(int style) {
		SkinInfo info = new SkinInfo();
		TypedArray a =
			mContext.obtainStyledAttributes(style, R.styleable.KeyboardStyle);

		int n = a.getIndexCount();
		int[] backgroundColor = {0xFF3C3C3C, 0xFF3C3C3C};
		boolean hasBgImage = false;
		boolean hasModTextColor = false;

		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);

			if (attr == R.styleable.KeyboardStyle_android_keyBackground) {
				info.keyBackground = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_modKeyBackground) {
				info.altKeyBackground = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_keyboardBackground) {
				info.background = a.getDrawable(attr);
				hasBgImage = true;
			} else if (attr == R.styleable.KeyboardStyle_android_keyTextColor) {
				info.textColor = a.getColor(attr, 0xFF000000);
			} else if (attr == R.styleable.KeyboardStyle_modTextColor) {
				info.textAltColor = a.getColor(attr, 0xFF000000);
				hasModTextColor = true;
			} else if (attr == R.styleable.KeyboardStyle_pressedTextColor) {
				info.pressedTextColor = a.getColor(attr, 0xFF000000);
			} else if (attr == R.styleable.KeyboardStyle_altLabelColor) {
				info.altLabelColor = a.getColor(attr, 0xFF202020);
			} else if (attr == R.styleable.KeyboardStyle_shadowColor) {
				int color = a.getColor(attr, 0);
				info.shadowColor = color == 0 ? null : color;
			} else if (attr == R.styleable.KeyboardStyle_altShadowColor) {
				int color = a.getColor(attr, 0);
				info.altShadowColor = color == 0 ? null : color;
			} else if (attr == R.styleable.KeyboardStyle_modShadowColor) {
				int color = a.getColor(attr, 0);
				info.modShadowColor = color == 0 ? null : color;
			} else if (attr == R.styleable.KeyboardStyle_colorBackgroundTop) {
				backgroundColor[0] = a.getColor(attr, 0xFF464646);
			} else if (attr == R.styleable.KeyboardStyle_colorBackgroundBottom) {
				backgroundColor[1] = a.getColor(attr, 0xFF464646);
			} else if (attr == R.styleable.KeyboardStyle_deleteKey) {
				info.deleteKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_returnKey) {
				info.returnKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_searchKey) {
				info.searchKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_shiftKey) {
				info.shiftKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_shiftLockedKey) {
				info.shiftLockedKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_spaceKey) {
				info.spaceKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_micKey) {
				info.micKey = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_leftArrow) {
				info.leftArrow = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_rightArrow) {
				info.rightArrow = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_upArrow) {
				info.upArrow = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_downArrow) {
				info.downArrow = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_boldLabel) {
				info.boldLabel = a.getBoolean(attr, false);
			} else if (attr == R.styleable.KeyboardStyle_smallKeys) {
				info.smallKeys = a.getBoolean(attr, false);
			} else if (attr == R.styleable.KeyboardStyle_padding) {
				info.padding = a.getInt(attr, 60);
			} else if (attr == R.styleable.KeyboardStyle_suggestBackground) {
				info.suggestBackground = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_suggestDivider) {
				info.suggestDivider = a.getDrawable(attr);
			} else if (attr == R.styleable.KeyboardStyle_candidateNormalColor) {
				int color = a.getColor(attr, 0);
				info.candidateNormalColor = color == 0 ? null : color;
			} else if (attr == R.styleable.KeyboardStyle_candidateRecommendedColor) {
				int color = a.getColor(attr, 0);
				info.candidateRecommendedColor = color == 0 ? null : color;
			} else if (attr == R.styleable.KeyboardStyle_candidateOtherColor) {
				int color = a.getColor(attr, 0);
				info.candidateOtherColor = color == 0 ? null : color;
				
			}
		}
		if (!hasBgImage) {
			info.background = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, backgroundColor);
		}
		if (!hasModTextColor) {
			info.textAltColor = info.textColor;
		}
		return info;
	}
	
	private SkinInfo loadBKSkin(String packageName) {
		SkinInfo info = new SkinInfo();
		Resources defResources = mContext.getResources();
		try {
			if (DEBUG) Log.d(TAG, "Loading skin " + packageName);
			Resources res = mContext.getPackageManager().getResourcesForApplication(packageName);
			
			info.background = getDrawable(res, "keyboard_background", packageName);
			if (info.background == null) {
				info.background = new ColorDrawable(0xff000000);
			}
			// Check for newFormat
			boolean isNewFormat = false;
			int idNewFormat = res.getIdentifier("newformat2", "bool", packageName);
			if (idNewFormat != 0) {
				isNewFormat = res.getBoolean(idNewFormat);
			}
			if (isNewFormat) {
				// New format
				Log.i(TAG, "Protected BK skin!");
				return loadBuiltinSkin(R.style.iPhone);
			}
			info.keyBackground = getDrawableOrDef(res, "btn_keyboard_key", packageName, 
					R.drawable.android_btn_keyboard_key);
			info.altKeyBackground = getDrawable(res, "btn_keyboard_key_alt", packageName);
			if (info.altKeyBackground == null) {
				info.altKeyBackground = info.keyBackground;
			}
			info.deleteKey = getDrawableOrDef(res, "sym_keyboard_delete", packageName, 
					R.drawable.android_sym_keyboard_delete);
			info.shiftKey = getDrawableOrDef(res, "sym_keyboard_shift", packageName, 
					R.drawable.android_sym_keyboard_shift);
			info.shiftLockedKey = getDrawableOrDef(res, "sym_keyboard_shift_locked", packageName, 
					R.drawable.android_sym_keyboard_shift);
			info.returnKey = getDrawableOrDef(res, "sym_keyboard_return", packageName, 
					R.drawable.android_sym_keyboard_return);
			info.searchKey = getDrawableOrDef(res, "sym_keyboard_search", packageName, 
					R.drawable.android_sym_keyboard_search);
			info.spaceKey = getDrawableOrDef(res, "sym_keyboard_space", packageName, 
					R.drawable.android_sym_keyboard_space);
			info.micKey = getDrawable(res, "ic_btn_speak_now", packageName);
			info.textColor = getColor(res, "text_color", packageName);
			info.pressedTextColor = getColor(res, "text_color", packageName);
			info.altLabelColor = getColor(res, "text_color2", packageName);
			info.textAltColor = getColor(res, "text_alt_color", packageName);
			// Guess if white or black icons must be used
			int color = info.textColor;
			int level = ((color >> 16) & 0xff) + ((color >> 8) & 0xff) + (color & 0xff);
			if (level >= 3*128) {
				// White version
				info.upArrow = defResources.getDrawable(R.drawable.up_arrow_white);
				info.downArrow = defResources.getDrawable(R.drawable.down_arrow_white);
				info.leftArrow = defResources.getDrawable(R.drawable.left_arrow_white);
				info.rightArrow = defResources.getDrawable(R.drawable.right_arrow_white);
				if (info.micKey == null) {
					info.micKey = defResources.getDrawable(R.drawable.mic_white);
				}
			} else {
				// Black version
				info.upArrow = defResources.getDrawable(R.drawable.up_arrow);
				info.downArrow = defResources.getDrawable(R.drawable.down_arrow);
				info.leftArrow = defResources.getDrawable(R.drawable.left_arrow);
				info.rightArrow = defResources.getDrawable(R.drawable.right_arrow);
				if (info.micKey == null) {
					info.micKey = defResources.getDrawable(R.drawable.mic_black);
				}
			}
			info.boldLabel = false;
			info.smallKeys = false;
			info.padding = 60;
		} catch (NameNotFoundException e) {
			Log.e("SmartKeyboard", "Failed to load skin" + packageName);
			e.printStackTrace();
			return loadBuiltinSkin(R.style.iPhone);
		}
		return info;
	}
	
	private String getSkinCache() {
		try {
			mContext.openFileInput(SKIN_CACHE);
			return mContext.getFileStreamPath(SKIN_CACHE).getAbsolutePath();
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	private SkinInfo loadOpenSkin(String path) {
		// Check if a cached skin exists
		String cachedSkin = getSkinCache();
		if (cachedSkin != null) {
			Log.d(TAG, "Skin cache found: using it instead of " + path);
			path = cachedSkin;
		} else {
			// So, try to create a cache
			Log.d(TAG, "No skin cache found. Trying to create it now.");
			cacheOpenSkin(mContext, path);
			cachedSkin = getSkinCache();
			if (cachedSkin != null) {
				Log.d(TAG, "Skin cache found: using it instead of " + path);
				path = cachedSkin;
			} else {
				Log.d(TAG, "Still no skin cache found. Give up.");
			}
		}
		Log.i(TAG, "Trying to load open skin: " + path);
		// just in case something goes wrong
		SkinInfo skin = loadBuiltinSkin(R.style.Black); 
		OpenSkin openSkin = new OpenSkin(mContext, path);
		if (openSkin.isValid()) {
			skin.background = openSkin.getBackground();
			skin.keyBackground = openSkin.getKeyBackground();
			skin.altKeyBackground = openSkin.getSpecialKeyBackground();
			skin.deleteKey = openSkin.getDeleteKey();
			skin.returnKey = openSkin.getReturnKey();
			skin.searchKey = openSkin.getSearchKey();
			skin.spaceKey = openSkin.getSpaceKey();
			skin.shiftKey = openSkin.getShiftKey();
			skin.shiftLockedKey = openSkin.getShiftLockedKey();
			skin.micKey = openSkin.getMicKey();
			skin.textColor = openSkin.getLabelColor();
			skin.altLabelColor = openSkin.getAltLabelColor();
			skin.textAltColor = openSkin.getModLabelColor();
			skin.shadowColor = openSkin.getShadowColor();
			skin.altShadowColor = openSkin.getAltShadowColor();
			skin.modShadowColor = openSkin.getModShadowColor();
			skin.boldLabel = openSkin.getBoldLabel();
			skin.suggestBackground = openSkin.getCandidatesBackground();
			skin.suggestDivider = openSkin.getCandidatesDivider();
			skin.candidateHighlightBackground = openSkin.getCandidateHighlightBackground();
			skin.candidateNormalColor = openSkin.getCandidatesNormalColor();
			skin.candidateRecommendedColor = openSkin.getCandidatesRecommendedColor();
			skin.candidateOtherColor = openSkin.getCandidatesOtherColor();
			skin.candidateHighlightColor = openSkin.getCandidatesHighlightColor();
			skin.labelFont = openSkin.getLabelFont();
		}
		return skin;
	}
	
	private Drawable getDrawable(Resources res, String name, String packageName) {
		try {
			int id = res.getIdentifier(name, "drawable", packageName);
			return res.getDrawable(id);
		} catch (Resources.NotFoundException e) {
			return null;
		}
	}
	
	private Drawable getDrawableOrDef(Resources res, String name, String packageName, 
			int resId) {
		try {
			int id = res.getIdentifier(name, "drawable", packageName);
			return res.getDrawable(id);
		} catch (Resources.NotFoundException e) {
			if (DEBUG) Log.d(TAG, "drawable not found: " + name);
			return mContext.getResources().getDrawable(resId);
		}
	}
	
	private int getColor(Resources res, String name, String packageName) {
		try {
			int id = res.getIdentifier(name, "color", packageName);
			return res.getColor(id);
		} catch (Resources.NotFoundException e) {
			return 0xffffffff;
		}
	}
	
	public class SkinInfo {
		public Drawable background;
		public Drawable keyBackground;
		public Drawable altKeyBackground;
		public Drawable deleteKey;
		public Drawable shiftKey;
		public Drawable shiftLockedKey;
		public Drawable returnKey;
		public Drawable searchKey;
		public Drawable spaceKey;
		public Drawable micKey;
		public Drawable upArrow;
		public Drawable downArrow;
		public Drawable leftArrow;
		public Drawable rightArrow;
		public int textColor;
		public int pressedTextColor;
		public int altLabelColor;
		public int textAltColor;
		public Integer shadowColor;
		public Integer altShadowColor;
		public Integer modShadowColor;
		public boolean boldLabel;
		public boolean smallKeys;
		public int padding;
		public SkinInfo popupSkin;
		public Drawable suggestBackground;
		public Drawable suggestDivider;
		public Drawable candidateHighlightBackground;
		public Integer candidateNormalColor;
		public Integer candidateRecommendedColor;
		public Integer candidateOtherColor;
		public Integer candidateHighlightColor;
		public Typeface labelFont;
	}

}
