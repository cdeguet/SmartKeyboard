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

import java.util.Calendar;

class TrialPolicy {

    private static final int HOUR = 3600 * 1000;
    private static final int DAY = 24 * HOUR;
    private static final long TRIAL_DELAY = 6 * DAY + 2 * HOUR;

    private static final String LAST_TRIAL_POPUP = "LAST_TRIAL_POPUP";
    private final Calendar calendar;
    private final SharedPreferences prefs;
    private boolean checked;

    TrialPolicy(Calendar calendar, SharedPreferences prefs) {
        this.calendar = calendar;
        this.prefs = prefs;
        this.checked = false;
    }

    boolean checkDisplayPopup() {
        if (checked) {
            return false;
        }
        checked = true;
        long prevTime = prefs.getLong(LAST_TRIAL_POPUP, 0L);
        return checkPreviousTime(prevTime);
    }

    private boolean checkPreviousTime(long prevTime) {
        long currentTime = calendar.getTimeInMillis();
        if (prevTime == 0) {
            // Don't show the popup now but make sure it's shown next time
            savePopupTime(currentTime);
            return false;
        }
        if (delayHasExpired(prevTime, currentTime)) {
            savePopupTime(currentTime);
            return true;
        }
        return false;
    }

    private boolean delayHasExpired(long prevTime, long currentTime) {
        return currentTime - prevTime > TRIAL_DELAY || currentTime < prevTime;
    }

    private void savePopupTime(long currentTime) {
        prefs.edit().putLong(LAST_TRIAL_POPUP, currentTime).apply();
    }
}
