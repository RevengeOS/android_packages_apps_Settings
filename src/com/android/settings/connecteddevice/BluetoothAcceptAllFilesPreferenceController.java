/*
 * Copyright (C) 2018 Citrus-CAF
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

package com.android.settings.connecteddevice;

import android.content.pm.PackageManager;
import android.content.Context;
import android.provider.Settings;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;

/** Handles a toggle for a setting to turn on Accept all files over Bluetooth. * */
public class BluetoothAcceptAllFilesPreferenceController extends TogglePreferenceController
        implements PreferenceControllerMixin {

    static final String KEY_BLUETOOTH_ACCEPT_ALL_FILES = "bluetooth_accept_all_files";

    public BluetoothAcceptAllFilesPreferenceController(Context context) {
        super(context, KEY_BLUETOOTH_ACCEPT_ALL_FILES);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_BLUETOOTH)
                    ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public boolean isChecked() {
        return Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.BLUETOOTH_ACCEPT_ALL_FILES, 0) != 0;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        final int value = isChecked ? 1 : 0;
        return Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.BLUETOOTH_ACCEPT_ALL_FILES, value);
    }
}
