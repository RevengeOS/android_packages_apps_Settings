/*
 * Copyright (C) 2020 shagbag913
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

package com.android.settings.display;

import android.content.Context;
import android.hardware.display.AmbientDisplayConfiguration;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

public class AmbientDisplayParentPreferenceController extends BasePreferenceController {

    private static Boolean mIsAmbientAvailable;

    public AmbientDisplayParentPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
        mIsAmbientAvailable = isAmbientAvailable(context);
    }

    @Override
    public int getAvailabilityStatus() {
        return mIsAmbientAvailable ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public CharSequence getSummary() {
        boolean alwaysOnPref = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        boolean onChargePref = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ON_CHARGE, 0, UserHandle.USER_CURRENT) == 1;
        int summary;

        if (alwaysOnPref) {
            summary = R.string.ambient_display_always_title;
        } else if (onChargePref) {
            summary = R.string.ambient_display_on_charge_title;
        } else {
            summary = R.string.ambient_display_off_summary;
        }
        return mContext.getText(summary);
    }

    private static Boolean isAmbientAvailable(Context context) {
        if (mIsAmbientAvailable == null) {
            AmbientDisplayConfiguration config = new AmbientDisplayConfiguration(context);
            mIsAmbientAvailable = config.alwaysOnAvailableForUser(UserHandle.USER_CURRENT);
        }
        return mIsAmbientAvailable;
    }
}
