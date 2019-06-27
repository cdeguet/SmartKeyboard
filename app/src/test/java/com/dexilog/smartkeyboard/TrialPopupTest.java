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

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TrialPopupTest {

    private static final String LAST_TRIAL_POPUP = "LAST_TRIAL_POPUP";

    @Mock SharedPreferences prefs;
    @Mock SharedPreferences.Editor prefEditor;
    @Mock Calendar calendar;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private TrialPolicy trialPolicy;
    private long currentTime;

    @Before
    public void setUp() throws Exception {
        trialPolicy = new TrialPolicy(calendar, prefs);
        currentTime = getTimeMs(2017, 3, 15);
        when(prefs.edit()).thenReturn(prefEditor);
        when(prefEditor.putLong(anyString(), anyLong())).thenReturn(prefEditor);
        when(calendar.getTimeInMillis()).thenReturn(currentTime);
    }

    @Test
    public void dontShowPopupOnFirstLaunch() throws Exception {
        when(prefs.getLong(LAST_TRIAL_POPUP, 0L)).thenReturn(0L);
        assertThat(trialPolicy.checkDisplayPopup(), is(false));
        verify(prefEditor).putLong(LAST_TRIAL_POPUP, currentTime);
    }

    @Test
    public void dontShowPopupBeforeDelay() throws Exception {
        setLastPopupDay(15 - 6);
        checkDisplayPopup(false);
    }

    @Test
    public void showPopupAfterDelay() throws Exception {
        setLastPopupDay(15 - 7);
        checkDisplayPopup(true);
    }

    @Test
    public void showPopupAfterPhoneDateSetInPast() throws Exception {
        setLastPopupDay(15 + 3);
        checkDisplayPopup(true);
    }

    @Test
    public void dontCheckAgainWithinSameSession() throws Exception {
        setLastPopupDay(15 - 8);
        trialPolicy.checkDisplayPopup();
        assertThat(trialPolicy.checkDisplayPopup(), is(false));
    }

    private void checkDisplayPopup(boolean mustDisplay) {
        assertThat(trialPolicy.checkDisplayPopup(), is(mustDisplay));
        if (mustDisplay) {
            verify(prefEditor).putLong(LAST_TRIAL_POPUP, currentTime);
        } else {
            verify(prefEditor, never()).putLong(anyString(), anyLong());
        }
    }

    private void setLastPopupDay(int day) {
        long prevTime = getTimeMs(2017, 3, day);
        when(prefs.getLong(LAST_TRIAL_POPUP, 0L)).thenReturn(prevTime);
    }

    private long getTimeMs(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTimeInMillis();
    }
}
