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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

public class PermissionManager {
    public static final int PERMISSION_REQUEST = 1;

    public interface PermissionsResultCallback {
        void onRequestPermissionsResult(boolean allGranted);
    }

    private Context context;
    private int requestCodeId;
    private final Map<Integer, PermissionsResultCallback> requestIdToCallback = new HashMap<>();
    private static PermissionManager sInstance;

    public static synchronized PermissionManager get(Context context) {
        if (sInstance == null) {
            sInstance = new PermissionManager(context);
        }
        return sInstance;
    }

    private PermissionManager(Context context) {
        this.context = context;
    }

    private synchronized int getNextRequestId() {
        return ++requestCodeId;
    }

    public void checkWriteStoragePermission(final PermissionsResultCallback callback) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, callback);
    }

    public void checkReadStoragePermission(final PermissionsResultCallback callback) {
        if (Build.VERSION.SDK_INT < 23) {
            callback.onRequestPermissionsResult(true);
        } else {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, callback);
        }
    }

    public void checkRecordingPermission(final PermissionsResultCallback callback) {
        checkPermission(Manifest.permission.RECORD_AUDIO, callback);
    }

    public void checkContactsPermission(final PermissionsResultCallback callback) {
        checkPermission(Manifest.permission.READ_CONTACTS, callback);
    }

    // Return true if permission is granted, otherwise ask for it
    private void checkPermission(String permission, final PermissionsResultCallback callback) {
        if (Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
            askPermission(permission, callback);
        } else {
            callback.onRequestPermissionsResult(true);
        }

    }

    private void askPermission(String permission, final PermissionsResultCallback callback) {
        int requestId = getNextRequestId();
        requestIdToCallback.put(requestId, callback);
        startPermissionActivity(permission, requestId);
    }

    private void startPermissionActivity(String permission, int requestId) {
        final Intent intent = new Intent();
        intent.setClass(context, PermissionActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PermissionActivity.PERMISSIONS, new String[] { permission });
        intent.putExtra(PermissionActivity.PERMISSION_REQUEST_CODE, requestId);
        context.startActivity(intent);
    }

    public synchronized void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        PermissionsResultCallback permissionsResultCallback = requestIdToCallback.get(requestCode);
        requestIdToCallback.remove(requestCode);
        boolean allGranted = (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        permissionsResultCallback.onRequestPermissionsResult(allGranted);
    }
    
}
