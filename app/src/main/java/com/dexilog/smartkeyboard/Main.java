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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Main extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		TextView welcomeView = (TextView)findViewById(R.id.welcome_text);
		String appName = getResources().getString(R.string.app_name);
		Pattern p = Pattern.compile("@");
		Matcher m = p.matcher(getResources().getString(R.string.welcome));
		String welcome = m.replaceAll(appName);
		welcomeView.setText(welcome);
		
		Button settings = (Button)findViewById(R.id.settings);
		settings.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					startActivity(new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS));
				} catch (Exception e) {
					// Workaround for HTC Hero
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_MAIN);
					ComponentName com = new ComponentName("com.android.settings", "com.android.settings.LanguageSettings");
					intent.setComponent(com); 
					startActivity(intent);
				}
			}
		});
		
		Button close = (Button)findViewById(R.id.help);
		close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.dexilog.com/smartkeyboard/users-guide"));
				startActivity(intent);
			}
		});
	}

}
