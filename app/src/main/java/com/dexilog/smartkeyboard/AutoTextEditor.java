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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.database.Cursor;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;

public class AutoTextEditor extends ListActivity {

	private static final int DIALOG_EDIT = 0;
	private static final int CONTEXT_MENU_EDIT = Menu.FIRST;
	private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 1;
	private static final String SELECTION = AutoTextProvider._ID + "=?";

	private static final String EDITING_KEY = "key";
	private static final String EDITING_VALUE = "value";
	private static final String EDITING_ID = "id";
	
	private Cursor mCursor;
	private String mEditingKey;
	private String mEditingValue;
	private int mEditingId;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		// Restore state
		if (savedInstanceState != null) {
			mEditingId = savedInstanceState.getInt(EDITING_ID);
			mEditingKey = savedInstanceState.getString(EDITING_KEY);
			mEditingValue = savedInstanceState.getString(EDITING_VALUE);
		}

		setContentView(R.layout.autotext_editor);
		// Add click listener on add button
		View addView = findViewById(R.id.autotext_add);
		addView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// Reset editing attributes
				mEditingId = -1;
				mEditingKey = mEditingValue = "";
				showDialog(DIALOG_EDIT);
			}
		});

		mCursor = managedQuery(AutoTextProvider.CONTENT_URI, null, null, null, AutoTextProvider.KEY);

		ListAdapter adapter = new Adapter(this, mCursor);
		setListAdapter(adapter);

		ListView listView = getListView();
		listView.setFastScrollEnabled(true);
		registerForContextMenu(listView);
	}
	
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(EDITING_ID, mEditingId);
		outState.putString(EDITING_KEY, mEditingKey);
		outState.putString(EDITING_VALUE, mEditingValue);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		View content = getLayoutInflater().inflate(R.layout.autotext_add, null);
		final EditText editKey = (EditText) content.findViewById(R.id.autotext_key);
		final EditText editValue = (EditText) content.findViewById(R.id.autotext_value);
		// No prediction in soft keyboard mode. TODO: Create a better way to disable prediction
		editKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
		editValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
				| InputType.TYPE_TEXT_FLAG_MULTI_LINE);

		return new AlertDialog.Builder(this)
		.setTitle(R.string.edit_autotext)
		.setView(content)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				final String key = editKey.getEditableText().toString();
				final String value = editValue.getEditableText().toString();
				if (key.length() > 0 && value.length() > 0) {
					addOrUpdateAutotext(mEditingId, key, value);
				}
			}})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}})
				.create();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog d) {
		final EditText editKey = (EditText) d.findViewById(R.id.autotext_key);
		final EditText editValue = (EditText) d.findViewById(R.id.autotext_value);
		editKey.setText(mEditingKey);
		editValue.setText(mEditingValue);
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		openContextMenu(v);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (!(menuInfo instanceof AdapterContextMenuInfo)) return;
		
		AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
		
		// Get current key
		mCursor.moveToPosition(adapterMenuInfo.position);
		final String key = mCursor.getString(
				mCursor.getColumnIndexOrThrow(AutoTextProvider.KEY));
		
		menu.setHeaderTitle(key);
		menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.autotext_context_edit);
		menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.autotext_context_delete);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ContextMenuInfo menuInfo = item.getMenuInfo();
		if (!(menuInfo instanceof AdapterContextMenuInfo)) return false;

		AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
		mCursor.moveToPosition(adapterMenuInfo.position);

		mEditingId = mCursor.getInt(
				mCursor.getColumnIndexOrThrow(AutoTextProvider._ID));
		mEditingKey = mCursor.getString(
				mCursor.getColumnIndexOrThrow(AutoTextProvider.KEY));
		mEditingValue = mCursor.getString(
				mCursor.getColumnIndexOrThrow(AutoTextProvider.VALUE));

		switch (item.getItemId()) {
		case CONTEXT_MENU_DELETE:
			delAutotext(mEditingId);
			return true;

		case CONTEXT_MENU_EDIT:
			showDialog(DIALOG_EDIT);
			return true;
		}

		return false;
	}

	private void addOrUpdateAutotext(int id, String key, String value) {
		ContentValues values = new ContentValues();
		values.put(AutoTextProvider.KEY, key);
		values.put(AutoTextProvider.VALUE, value);
		if (id == -1) {
			getContentResolver().insert(AutoTextProvider.CONTENT_URI, values);
		}
		else {
			getContentResolver().update(AutoTextProvider.CONTENT_URI, values,
					SELECTION, new String[] { Integer.toString(id) });
		}
		mCursor.requery();
	}

	private void delAutotext(int id) {
		getContentResolver().delete(AutoTextProvider.CONTENT_URI, SELECTION, 
				new String[] { Integer.toString(id) });
		mCursor.requery();
	}

	static private class Adapter extends ResourceCursorAdapter {

		private int mKeyIndex;
		private int mValueIndex;

		public Adapter(Context context, Cursor cursor) {
			super(context, android.R.layout.simple_list_item_1, cursor);
			mKeyIndex = cursor.getColumnIndex(AutoTextProvider.KEY);
			mValueIndex = cursor.getColumnIndex(AutoTextProvider.VALUE);
		}

		@Override 
		public void bindView(View view, Context context, Cursor cursor) {
			StringBuilder text = new StringBuilder(cursor.getString(mKeyIndex));
			text.append(" -> ");
			text.append(cursor.getString(mValueIndex));
			((TextView)view).setText(text.toString());
		}
	}

}
