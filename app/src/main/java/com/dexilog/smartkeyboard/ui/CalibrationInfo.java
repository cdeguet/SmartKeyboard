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

import android.content.SharedPreferences;

public class CalibrationInfo {
	
	public int mPortraitDx = 0;
	public int mPortraitDy = 0;
	public int mPortraitSpace = 0;
	public int mLandscapeDx = 0;
	public int mLandscapeDy = 0;
	public int mLandscapeSpace = 0;
	
	public CalibrationInfo(SharedPreferences sp) {
		mPortraitDx = sp.getInt("calib_p_dx", 0);
		mPortraitDy = sp.getInt("calib_p_dy", 0);
		mPortraitSpace = sp.getInt("calib_p_space", -4);
		mLandscapeDx = sp.getInt("calib_l_dx", 0);
		mLandscapeDy = sp.getInt("calib_l_dy", 0);
		mLandscapeSpace = sp.getInt("calib_l_space", -6);
	}
}
