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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class LangKeyPref extends CheckBoxPreference {

	public LangKeyPref(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onClick() {
		super.onClick();
		
		Context context = getContext();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		List<String> langList = buildLangList(pref, context.getResources());
		// Check if there is only one language selected
		if (langList.size() == 1) {
			AlertDialog d = (new AlertDialog.Builder(context))
			.setTitle(android.R.string.dialog_alert_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage( (R.string.only_one_language))
			.setCancelable(true)
			.setPositiveButton(android.R.string.ok, new NullClickListener())
			.create();
			d.show();
			// Set the current language
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("curLang", langList.get(0));
			editor.apply();
		}
	}
	
	static public List<String> buildLangList(SharedPreferences sp, Resources res) {
		// Retrieve list of enabled languages for the lang key
		final StringBuilder sb = new StringBuilder(20);
		final List<String> langList = new ArrayList<String>();
		final String[] availLang = res.getStringArray(R.array.language_values);
		for (final String lang : availLang) {
			sb.setLength(0);
			sb.append("lang");
			sb.append(lang);
			if (sp.getBoolean(sb.toString(), false)) {
				// Add language
				langList.add(lang);
			}
		}
		return langList;
	}
	
	
	static private class NullClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {	
		}
	}
}
