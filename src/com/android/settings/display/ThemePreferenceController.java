/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.android.settings.display;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.text.TextUtils;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.wrapper.OverlayManagerWrapper;
import com.android.settings.wrapper.OverlayManagerWrapper.OverlayInfo;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.android.internal.logging.nano.MetricsProto.MetricsEvent.ACTION_THEME;

public class ThemePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_THEME = "theme";

    private final MetricsFeatureProvider mMetricsFeatureProvider;
    private final OverlayManagerWrapper mOverlayService;
    private final PackageManager mPackageManager;

    public ThemePreferenceController(Context context) {
        this(context, ServiceManager.getService(Context.OVERLAY_SERVICE) != null
                ? new OverlayManagerWrapper() : null);
    }

    @VisibleForTesting
    ThemePreferenceController(Context context, OverlayManagerWrapper overlayManager) {
        super(context);
        mOverlayService = overlayManager;
        mPackageManager = context.getPackageManager();
        mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @Override
    public String getPreferenceKey() {
        return KEY_THEME;
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_THEME.equals(preference.getKey())) {
            mMetricsFeatureProvider.action(mContext, ACTION_THEME);
        }
        return false;
    }

    @Override
    public void updateState(Preference preference) {
        ListPreference pref = (ListPreference) preference;
        String[] pkgs = getAvailableThemes();
        CharSequence[] labels = new CharSequence[pkgs.length];
        labels[0] = "Default";
        for (int i = 1; i < pkgs.length; i++) {
            try {
                labels[i] = mPackageManager.getApplicationInfo(pkgs[i], 0)
                        .loadLabel(mPackageManager);
            } catch (NameNotFoundException e) {
                labels[i] = pkgs[i];
            }
        }
        pref.setEntries(labels);
        pref.setEntryValues(pkgs);
        String theme = getCurrentTheme();
        CharSequence themeLabel = null;

        for (int i = 0; i < pkgs.length; i++) {
            if (TextUtils.equals(pkgs[i], theme)) {
                themeLabel = labels[i];
                break;
            }
        }

        pref.setSummary(themeLabel);
        pref.setValue(theme);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String current = getTheme();
        if (Objects.equals(newValue, current)) {
            return true;
        }
        if (Objects.equals(newValue, "default")) {
            mOverlayService.setEnabled(current, false, UserHandle.myUserId());
            return true;
        }
        mOverlayService.setEnabledExclusiveInCategory((String) newValue, UserHandle.myUserId());
        return true;
    }

    private boolean isTheme(OverlayInfo oi) {
        if (!OverlayInfo.CATEGORY_THEME.equals(oi.category)) {
            return false;
        }
        try {
            PackageInfo pi = mPackageManager.getPackageInfo(oi.packageName, 0);
            return pi != null && !pi.isStaticOverlayPackage();
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getTheme() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        for (int i = 0, size = infos.size(); i < size; i++) {
            if (infos.get(i).isEnabled() && isTheme(infos.get(i))) {
                return infos.get(i).packageName;
            }
        }
        return "default";
    }

    @Override
    public boolean isAvailable() {
        if (mOverlayService == null) return false;
        String[] themes = getAvailableThemes();
        return themes != null && themes.length > 1;
    }


    @VisibleForTesting
    String getCurrentTheme() {
        return getTheme();
    }

    @VisibleForTesting
    String[] getAvailableThemes() {
        List<OverlayInfo> infos = mOverlayService.getOverlayInfosForTarget("android",
                UserHandle.myUserId());
        final int size = infos.size();
        List<String> pkgs = new ArrayList<>(size + 1);
        pkgs.add("default");
        for (int i = 0; i < size; i++) {
            if (isTheme(infos.get(i))) {
                pkgs.add(infos.get(i).packageName);
            }
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }
}
