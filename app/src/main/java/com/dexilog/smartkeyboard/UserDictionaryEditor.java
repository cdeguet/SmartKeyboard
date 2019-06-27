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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;

import com.dexilog.smartkeyboard.input.WordComposerImpl;
import com.dexilog.smartkeyboard.lang.Korean;


public class UserDictionaryEditor extends ListActivity {

	private static final int DIALOG_EDIT = 0;
	private static final int CONTEXT_MENU_EDIT = Menu.FIRST;
	private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 1;
	private static final String SELECTION = UserDictionaryProvider._ID + "=?";

	private static final String EDITING_WORD = "word";
	private static final String EDITING_LANG = "lang";
	private static final String EDITING_ID = "id";
	
	private Cursor mCursor;
	private String mEditingWord;
	private String mEditingLang;
	private int mEditingId;
	private Korean mKorean;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		// Restore state
		if (savedInstanceState != null) {
			mEditingId = savedInstanceState.getInt(EDITING_ID);
			mEditingLang = savedInstanceState.getString(EDITING_LANG);
			mEditingWord = savedInstanceState.getString(EDITING_WORD);
		}

		setContentView(R.layout.userdic_editor);
		// Add click listener on add button
		View addView = findViewById(R.id.userdic_add);
		addView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// Reset editing attributes
				mEditingId = -1;
				mEditingWord = "";
				// Find current language
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(UserDictionaryEditor.this);
				mEditingLang = sp.getString("curLang", "EN").substring(0, 2);
				showDialog(DIALOG_EDIT);
			}
		});

		mCursor = managedQuery(UserDictionaryProvider.CONTENT_URI_CONVERTED_WORDS, null, null, null, UserDictionaryProvider.WORD);

		ListAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
				mCursor, new String[] { UserDictionaryProvider.WORD }, new int[] { android.R.id.text1 });
		setListAdapter(adapter);

		ListView listView = getListView();
		listView.setFastScrollEnabled(true);
		registerForContextMenu(listView);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		View content = getLayoutInflater().inflate(R.layout.userdic_add, null);
		final EditText editWord = (EditText) content.findViewById(R.id.userdic_word);
		final Spinner editLang = (Spinner) content.findViewById(R.id.userdic_lang);
		// No prediction in soft keyboard mode. TODO: Create a better way to disable prediction
		editWord.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
		            this, R.array.languages, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		editLang.setAdapter(adapter);

		return new AlertDialog.Builder(this)
		.setTitle(R.string.edit_word)
		.setView(content)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String word = editWord.getEditableText().toString();
				final String lang = editLang.getSelectedItem().toString();
				if (word.length() > 0) {
					addOrUpdateuserdic(mEditingId, word, lang);
				}
			}})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}})
				.create();
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(EDITING_ID, mEditingId);
		outState.putString(EDITING_WORD, mEditingWord);
		outState.putString(EDITING_LANG, mEditingLang);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog d) {
		final EditText editWord = (EditText) d.findViewById(R.id.userdic_word);
		final Spinner editLang = (Spinner) d.findViewById(R.id.userdic_lang);
		editWord.setText(mEditingWord);
		// Find language index
		final String lang = mEditingLang;
		final String [] languages = getResources().getStringArray(R.array.languages);
		for (int i=0; i<languages.length; i++) {
			if (languages[i].equals(lang)) {
				editLang.setSelection(i);
				break;
			}
		}
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		openContextMenu(v);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (!(menuInfo instanceof AdapterContextMenuInfo)) return;
		
		AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
		
		// Get current WORD
		mCursor.moveToPosition(adapterMenuInfo.position);
		final String WORD = mCursor.getString(
				mCursor.getColumnIndexOrThrow(UserDictionaryProvider.WORD));
		
		menu.setHeaderTitle(WORD);
		menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.edit_word);
		menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.delete_word);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = item.getMenuInfo();
		if (!(menuInfo instanceof AdapterContextMenuInfo)) return false;

		AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
		mCursor.moveToPosition(adapterMenuInfo.position);

		mEditingId = mCursor.getInt(
				mCursor.getColumnIndexOrThrow(UserDictionaryProvider._ID));
		mEditingWord = mCursor.getString(
				mCursor.getColumnIndexOrThrow(UserDictionaryProvider.WORD));
		mEditingLang = mCursor.getString(
				mCursor.getColumnIndexOrThrow(UserDictionaryProvider.LANG));

		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE:
			deluserdic(mEditingId);
			return true;

		case CONTEXT_MENU_EDIT:
			showDialog(DIALOG_EDIT);
			return true;
		}

		return false;
	}
	
	private Korean getKorean() {
		if (mKorean == null) {
			mKorean = new Korean(new WordComposerImpl());
		}
		return mKorean;
	}

	private void addOrUpdateuserdic(int id, String WORD, String LANG) {
		ContentValues values = new ContentValues();
		// Convert to jamo if Korean
		if (LANG.equals("KO")) {
			StringBuilder sb = new StringBuilder();
			getKorean().reverse(WORD, sb);
			WORD = sb.toString();
		}
		values.put(UserDictionaryProvider.WORD, WORD);
		values.put(UserDictionaryProvider.LANG, LANG);
		if (id == -1) {
			getContentResolver().insert(UserDictionaryProvider.CONTENT_URI_WORDS, values);
		}
		else {
			getContentResolver().update(UserDictionaryProvider.CONTENT_URI_WORDS, values,
					SELECTION, new String[] { Integer.toString(id) });
		}
		mCursor.requery();
	}

	private void deluserdic(int id) {
		getContentResolver().delete(UserDictionaryProvider.CONTENT_URI_WORDS, SELECTION, 
				new String[] { Integer.toString(id) });
		mCursor.requery();
	}

}
