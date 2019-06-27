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

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class LatinLayoutList extends ListPreference {
	
	public LatinLayoutList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		
		final Resources resources = getContext().getResources();
		final String[] langChoices = resources.getStringArray(R.array.language_choices);
		final String[] langValues = resources.getStringArray(R.array.language_values);
		final int length = langChoices.length;
		HashMap<String, String> langMap = new HashMap<String, String>();
		for (int i=0; i<length; i++) {
			langMap.put(langValues[i], langChoices[i]);
		}
		
		final CharSequence[] entryValues = getEntryValues();
		CharSequence[] entries = new CharSequence[entryValues.length];
		entries[0] = resources.getString(R.string.default_latin_lang);
		for (int i=1; i<entryValues.length; i++) {
			entries[i] = langMap.get(entryValues[i]);
		}
		setEntries(entries);
		
		super.onPrepareDialogBuilder(builder);
	}

}
