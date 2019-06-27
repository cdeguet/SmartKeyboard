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

package com.dexilog.smartkeyboard.settings;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

public class PermissionActivity extends Activity {
    public static final String PERMISSIONS = "PERMISSIONS";
    public static final String PERMISSION_REQUEST_CODE = "REQUEST_CODE";

    private static final int INVALID_REQUEST_CODE = -1;
    private int mPendingRequestCode = INVALID_REQUEST_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPendingRequestCode = (savedInstanceState != null)
                ? savedInstanceState.getInt(PERMISSION_REQUEST_CODE, INVALID_REQUEST_CODE)
                : INVALID_REQUEST_CODE;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PERMISSION_REQUEST_CODE, mPendingRequestCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only do request when there is no pending request to avoid duplicated requests.
        if (mPendingRequestCode == INVALID_REQUEST_CODE) {
            final Bundle extras = getIntent().getExtras();
            final String[] permissionsToRequest = extras.getStringArray(PERMISSIONS);
            mPendingRequestCode = extras.getInt(PERMISSION_REQUEST_CODE);
            // Assuming that all supplied permissions are not granted yet, so that we don't need to
            ActivityCompat.requestPermissions(this, permissionsToRequest, mPendingRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        mPendingRequestCode = INVALID_REQUEST_CODE;
        finish();
        PermissionManager.get(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}