/*
 * Copyright (C) 2019 RevengeOS
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

import static android.content.Context.UI_MODE_SERVICE;

import android.app.UiModeManager;
import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class UiPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private final String KEY_UI_SETTINGS = "ui_preference_screen";
    private final ThemePreferenceController themeController;
    private final UiModeManager mUiModeManager;
    private String lightThemeLabel;
    private String darkStyle[];

    public UiPreferenceController(Context context) {
        super(context);
        themeController = new ThemePreferenceController(context);
        mUiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        lightThemeLabel = context.getResources().getString(R.string.systemui_theme_light);
        darkStyle = context.getResources().getStringArray(R.array.dark_theme_style_entries);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        CharSequence mUiStyle;
        if (mUiModeManager.getNightMode() != 1) {
            final int value = Settings.System.getInt(mContext.getContentResolver(), Settings.System.DARK_THEME_STYLE, 0);
            mUiStyle = darkStyle[value];
        } else {
            mUiStyle = lightThemeLabel;
        }
        final CharSequence uiSummary = themeController.getCurrentThemeLabel() + " " + mUiStyle;
        preference.setSummary(uiSummary);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_UI_SETTINGS;
    }
}
