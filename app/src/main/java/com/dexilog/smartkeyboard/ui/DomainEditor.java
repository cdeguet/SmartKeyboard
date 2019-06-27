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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.dexilog.smartkeyboard.R;


public class DomainEditor extends ListActivity {
	
	private static final int DIALOG_EDIT = 0;
	public static final int[] DOMAINS = {R.string.popular_domain_0, 
		R.string.popular_domain_1, R.string.popular_domain_2, R.string.popular_domain_3};
	static final String KEY = "key";
	static final String DOMAIN = "domain";

	ArrayList<HashMap<String,String>> mValues;
	int mCurDomain;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		final Resources res = getResources();
		mValues = new ArrayList<HashMap<String,String>>();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		for (int i=0; i<DOMAINS.length; i++) {
			final String key = "domain_" + Integer.toString(i);
			final String domain = sp.getString(key, res.getString(DOMAINS[i]));
			HashMap<String,String> item = new HashMap<String,String>();
			item.put(KEY, key);
			item.put(DOMAIN, domain);
			mValues.add(item);
		}

		SimpleAdapter toolsAdapter =
			new SimpleAdapter(this, mValues, android.R.layout.simple_list_item_1,
					new String[] { DOMAIN }, new int[] { android.R.id.text1 } );
		setListAdapter( toolsAdapter );

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		View content = getLayoutInflater().inflate(R.layout.edit_domain, null);
		final EditText domain = (EditText) content.findViewById(R.id.domain);
		// No prediction in soft keyboard mode. TODO: Create a better way to disable prediction
		domain.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

		return new AlertDialog.Builder(this)
		.setTitle(R.string.edit_domain)
		.setView(content)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String newDomain = domain.getEditableText().toString();
				if (newDomain.length() > 0) {
					updateDomain(mCurDomain, newDomain);
				}
			}})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}})
		.create();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog d) {
		final EditText domain = (EditText) d.findViewById(R.id.domain);
		domain.setText(mValues.get(mCurDomain).get(DOMAIN));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mCurDomain = position;
		showDialog(DIALOG_EDIT);
	}
	
	private void updateDomain(int pos, final String newDomain) {
		HashMap<String,String> item = mValues.get(pos);
		item.put(DOMAIN, newDomain);
		final String key = item.get(KEY);
		SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
		edit.putString(key, newDomain);
		edit.commit();
	}
}
