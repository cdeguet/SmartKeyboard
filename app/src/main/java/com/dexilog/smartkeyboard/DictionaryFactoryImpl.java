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
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.util.Log;

import com.dexilog.smartkeyboard.suggest.AutoText;
import com.dexilog.smartkeyboard.suggest.Dictionary;
import com.dexilog.smartkeyboard.suggest.DictionaryFactory;
import com.dexilog.smartkeyboard.suggest.SmartDictionary;
import com.dexilog.smartkeyboard.suggest.UserDictionary;

class DictionaryFactoryImpl implements DictionaryFactory {

	static private final String TAG = "SmartKeyboardPro";
	private Context mContext;
	
	// Dictionary cache
	private Map<String, Dictionary> mLangDicts;
	private Map<String, UserDictionary> mUserDicts;
	private Map<String, AutoText> mAutoTexts;
	private Map<String, SmartDictionary> mSmartDics;
	
	public DictionaryFactoryImpl(Context context) {
		mContext = context;
		mLangDicts = new HashMap<String, Dictionary>();
		mUserDicts = new HashMap<String, UserDictionary>();
		mAutoTexts = new HashMap<String, AutoText>();
		mSmartDics = new HashMap<String, SmartDictionary>();
	}

	@Override
	public Dictionary getLangDictionary(String lang) throws IOException {
		if (mLangDicts.containsKey(lang)) {
			return mLangDicts.get(lang);
		} else {
			Log.i(TAG, "Trying to load dictionary: " + lang);
			Resources res = null;
			final String langName = lang.toLowerCase();
			final String pkgName = "net.cdeguet.smartkeyboardpro." + langName;
			if (langName.equals("jp")) {
				try {
					Japanese dic = new Japanese(mContext);
					mLangDicts.put(lang, dic);
					return dic;
				} catch (Exception e) {
					return null;
				}
			} else if (langName.equals("zh")) {
				try {
					Chinese dic = new Chinese();
					dic.initPinyinEngine(mContext);
					mLangDicts.put(lang, dic);
					return dic;
				} catch (Exception e) {
					return null;
				}
			} else {
				try {
					res = mContext.getPackageManager().getResourcesForApplication(pkgName);
				} catch (NameNotFoundException e) {
					return null;
				}
			}
			BinaryDictionary dic = null;
			if (res != null) {
				AssetFileDescriptor fd = res.getAssets().openFd(langName + "_dic.mp3");
				dic = new BinaryDictionary(fd);
				fd.close();
				mLangDicts.put(lang, dic);
			}
			return dic;
		}
	}

    @Override
    public AutoText getAutoText(String lang) throws IOException {
		if (mAutoTexts.containsKey(lang)) {
			return mAutoTexts.get(lang);
		} else {
			Resources res = null;
			int xml = 0;
			final String langName = lang.toLowerCase();
			final String pkgName = "net.cdeguet.smartkeyboardpro." + langName;
			if (langName.equals("en")) {
				res = mContext.getResources();
				xml = R.xml.autotext; 
			} else {
				try {
					res = mContext.getPackageManager().getResourcesForApplication(pkgName);
					xml = res.getIdentifier("autotext", "xml", pkgName);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
			AutoText autotext = null;
			if (res != null && xml != 0) {
				autotext = new AutoText(res, xml);
				mAutoTexts.put(lang, autotext);
			}
			return autotext;
		}
	}

    @Override
    public UserDictionary getUserDictionary(String lang) {
		if (mUserDicts.containsKey(lang)) {
			return mUserDicts.get(lang);
		} else {
			UserDictionary dic = new UserDictionaryImpl(mContext, lang);
			mUserDicts.put(lang, dic);
			return dic;
		}
	}

    @Override
	public SmartDictionary getSmartDictionary(String lang) {
		if (mSmartDics.containsKey(lang)) {
			return mSmartDics.get(lang);
		} else {
			SmartDictionary dic = new SmartDictionaryImpl(mContext, lang);
			mSmartDics.put(lang, dic);
			return dic;
		}
	}
}
