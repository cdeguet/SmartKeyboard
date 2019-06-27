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

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;  
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class About extends DialogPreference {

	private Context mContext;
	private LinearLayout mLayout;
	private TextView mText;

	public About(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	protected void onPrepareDialogBuilder(Builder builder) {

		mLayout = new LinearLayout(mContext); 
		mLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)); 
		mLayout.setMinimumWidth(400); 
		mLayout.setPadding(20, 20, 20, 20);

		mText = new TextView(mContext);

		final String aboutMsg = mContext.getResources().getString(R.string.about_msg);
		StringBuilder msg = new StringBuilder("This is Smart Keyboard Pro ");
		try {
			final String version = mContext.getPackageManager().getPackageInfo("net.cdeguet.smartkeyboardpro", 0).versionName;
			msg.append(version);
		} catch (Exception e) {
		}

		msg.append('\n');
		msg.append(aboutMsg);
		mText.setText(msg.toString());

		mLayout.addView(mText); 
		builder.setView(mLayout); 
	} 

}
