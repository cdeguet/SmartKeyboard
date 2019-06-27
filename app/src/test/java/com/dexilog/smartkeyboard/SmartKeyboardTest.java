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

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SmartKeyboardTest {

    SmartKeyboard smartKeyboard;

    @Mock
    SharedPreferences prefs;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        smartKeyboard = new SmartKeyboard();
        smartKeyboard.mSharedPref = prefs;
    }

    @Test
    public void testLaunchSettingsDisabled() {
        when(prefs.getBoolean("disable_settings", false)).thenReturn(true);
        assertEquals(false, smartKeyboard.launchSettings());
    }

    @Test(expected = NullPointerException.class)
    public void testLaunchSettingsEnabled() {
        when(prefs.getBoolean("disable_settings", false)).thenReturn(false);
        // throw null pointer as due to mKeyboardView.closing();
        smartKeyboard.launchSettings();
    }
}
