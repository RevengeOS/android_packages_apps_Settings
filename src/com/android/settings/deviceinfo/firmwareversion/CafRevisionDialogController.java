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

package com.android.settings.deviceinfo.firmwareversion;

import android.os.SystemProperties;
import android.support.annotation.VisibleForTesting;

import com.android.settings.R;

public class CafRevisionDialogController {

    @VisibleForTesting
    static final int CAF_REVISION_VALUE_ID = R.id.caf_revision_value;
    private static final String CAF_REVISION_PROP = "ro.caf.revision";

    private final FirmwareVersionDialogFragment mDialog;

    public CafRevisionDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
    }

    /**
     * Set CAF Revision in dialog
     */
    public void initialize() {
        final String cafRevisionValue = SystemProperties.get(CAF_REVISION_PROP);
        mDialog.setText(CAF_REVISION_VALUE_ID, cafRevisionValue);
    }
}
