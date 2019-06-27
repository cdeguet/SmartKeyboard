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

import android.view.MotionEvent;

class MotionEventWrapper {
	
	static {
		try {
			final Class<?> param1[] = {};
			MotionEvent.class.getDeclaredMethod("getPointerCount", param1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static void checkAvailable() {}
	
	static public int getPointerCount(MotionEvent me) {
		return me.getPointerCount();
	}
	
	static public int findPointerIndex(MotionEvent me, int pointerId) {
		return me.findPointerIndex(pointerId);
	}
	
	static public float getX(MotionEvent me, int pointerIndex) {
		return me.getX(pointerIndex);
	}
	
	static public float getY(MotionEvent me, int pointerIndex) {
		return me.getY(pointerIndex);
	}
}
