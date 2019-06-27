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

import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

public class LangListPref extends ListPreference {

	public LangListPref(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
    protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		if (!positiveResult)
			return;
		
		final Context context = getContext();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		// Check if the language is defined in the language key
		List<String> langList = LangKeyPref.buildLangList(pref, context.getResources());
		// Check if there is only one language selected
		if (langList.size() == 1) {
			String curLang = pref.getString("curLang", "EN");
			if (!curLang.equals(langList.get(0))) {
				AlertDialog d = (new AlertDialog.Builder(context))
				.setTitle(android.R.string.dialog_alert_title)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage( (R.string.different_language))
				.setCancelable(true)
				.setPositiveButton(android.R.string.ok, new NullClickListener())
				.create();
				d.show();
			}
		}
	}
	
	static private class NullClickListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface dialog, int which) {	
		}
	}

}
