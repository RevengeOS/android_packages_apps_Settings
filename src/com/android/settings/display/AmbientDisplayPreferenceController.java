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

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.widget.RadioButtonPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

public class AmbientDisplayPreferenceController extends AbstractPreferenceController implements
        RadioButtonPreference.OnClickListener, PreferenceControllerMixin, LifecycleObserver,
        OnPause, OnResume {

    static final String KEY_ON_CHARGE = "doze_on_charge";
    static final String KEY_ALWAYS_ON = "doze_always_on";
    static final String KEY = "ambient_display_category";

    private Context mContext;
    private SettingObserver mSettingObserver;
    private PreferenceCategory mPreferenceCategory;
    private RadioButtonPreference mAlwaysOnPref;
    private RadioButtonPreference mOnChargePref;

    public AmbientDisplayPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        mContext = context;

        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreferenceCategory = screen.findPreference(getPreferenceKey());
            mAlwaysOnPref = makeRadioPreference(KEY_ALWAYS_ON, R.string.ambient_display_always_title);
            mOnChargePref = makeRadioPreference(KEY_ON_CHARGE, R.string.ambient_display_on_charge_title);

            if (mPreferenceCategory != null) {
                mSettingObserver = new SettingObserver(mPreferenceCategory);
            }
        }
    }

    @Override
    public void onResume() {
        if (mSettingObserver != null) {
            mSettingObserver.register(mContext.getContentResolver());
        }
    }

    @Override
    public void onPause() {
        if (mSettingObserver != null) {
            mSettingObserver.unregister(mContext.getContentResolver());
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference preference) {
        int alwaysOnSetting;
        int onChargeSetting;
        String prefKey = preference.getKey();
        if (prefKey == KEY_ALWAYS_ON) {
            alwaysOnSetting = 1;
            onChargeSetting = 0;
        } else {
            alwaysOnSetting = 0;
            onChargeSetting = 1;
        }
        Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON, alwaysOnSetting, UserHandle.USER_CURRENT);
        Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ON_CHARGE, onChargeSetting, UserHandle.USER_CURRENT);
    }

    @Override
    public void updateState(Preference preference) {
        boolean alwaysOnSetting = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        boolean onChargeSetting = Settings.Secure.getIntForUser(mContext.getContentResolver(),
                Settings.Secure.DOZE_ON_CHARGE, 0, UserHandle.USER_CURRENT) == 1;

        if (mAlwaysOnPref.isChecked() != alwaysOnSetting) {
            mAlwaysOnPref.setChecked(alwaysOnSetting);
        }

        if (mOnChargePref.isChecked() != onChargeSetting) {
            mOnChargePref.setChecked(onChargeSetting);
        }

        if (alwaysOnSetting == false && onChargeSetting == false) {
            mOnChargePref.setEnabled(false);
            mAlwaysOnPref.setEnabled(false);
        } else {
            mOnChargePref.setEnabled(true);
            mAlwaysOnPref.setEnabled(true);
        }
    }

    private RadioButtonPreference makeRadioPreference(String key, int titleId) {
        RadioButtonPreference pref = new RadioButtonPreference(mPreferenceCategory.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnClickListener(this);
        mPreferenceCategory.addPreference(pref);
        return pref;
    }

    class SettingObserver extends ContentObserver {
        private final Uri DOZE_ALWAYS_ON_URI =
                Settings.Secure.getUriFor(Settings.Secure.DOZE_ALWAYS_ON);
        private final Uri DOZE_ON_CHARGE_URI =
                Settings.Secure.getUriFor(Settings.Secure.DOZE_ON_CHARGE);

        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            mPreference = preference;
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(DOZE_ALWAYS_ON_URI, false, this);
            cr.registerContentObserver(DOZE_ON_CHARGE_URI, false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (DOZE_ALWAYS_ON_URI.equals(uri) || DOZE_ON_CHARGE_URI.equals(uri)) {
                updateState(mPreference);
            }
        }
    }
}
