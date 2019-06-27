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

package com.dexilog.smartkeyboard.keyboard;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.dexilog.smartkeyboard.ui.DomainEditor;
import com.dexilog.smartkeyboard.ui.SmileyEditor;

public class CustomKeys {

	private static final String SMILEY = "%smiley_";
	private static final String DOMAIN = "%domain_";
	
	private ArrayList<String> mSmileys = new ArrayList<String>();
	private ArrayList<String> mDomains = new ArrayList<String>();
	private HashMap<String, String> mCache = new HashMap<String,String>();
	private Context mContext;
	private SharedPreferences mPref;
	
	public CustomKeys(Context context, SharedPreferences pref) {
		mContext = context;
		mPref = pref;
		reload();
	}
	
	public void reload() {
		mCache.clear();
		mSmileys.clear();
		mDomains.clear();
		StringBuilder sb = new StringBuilder();
		final String[] smileys = SmileyEditor.SMILEYS;
		SharedPreferences sp = mPref;
		final Resources res = mContext.getResources();
		for (int i=0; i<smileys.length; i++) {
			sb.setLength(0);
			sb.append("smiley_");
			sb.append(i);
			final String smiley = sp.getString(sb.toString(), smileys[i]);
			mSmileys.add(smiley);
		}
		final int[] domains = DomainEditor.DOMAINS;
		for (int i=0; i<domains.length; i++) {
			sb.setLength(0);
			sb.append("domain_");
			sb.append(i);
			final String domain = sp.getString(sb.toString(), res.getString(domains[i]));
			mDomains.add(domain);
		}
	}
	
	public String translate(String label) {
		if (mCache.containsKey(label)) {
			return mCache.get(label);
		} else {
			String newLabel = label;
			if (label.startsWith(SMILEY)) {
				int index = Integer.parseInt(label.substring(8, 10));
				newLabel = mSmileys.get(index);
				if (label.length() > 9) {
					newLabel = newLabel + label.substring(10);
				}
			} else if (label.startsWith(DOMAIN)) {
				int index = Integer.parseInt(label.substring(8, 10));
				newLabel = mDomains.get(index);
				if (label.length() > 9) {
					newLabel = newLabel + label.substring(10);
				}
			}
			mCache.put(label, newLabel);
			return newLabel;
		}
	}
	
}
