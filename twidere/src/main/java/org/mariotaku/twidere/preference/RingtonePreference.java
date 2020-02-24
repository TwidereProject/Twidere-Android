/*
 * Copyright (C) 2008 The Android Open Source Project
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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.RingtoneManager;
import androidx.preference.Preference;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;

public class RingtonePreference extends Preference {

    private final int mRingtoneType;
    private final boolean mShowDefault;
    private final boolean mShowSilent;

    public RingtonePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference);
        mRingtoneType = a.getInt(R.styleable.RingtonePreference_android_ringtoneType,
                RingtoneManager.TYPE_RINGTONE);
        mShowDefault = a.getBoolean(R.styleable.RingtonePreference_android_showDefault, true);
        mShowSilent = a.getBoolean(R.styleable.RingtonePreference_android_showSilent, true);
        a.recycle();
    }

    public int getRingtoneType() {
        return mRingtoneType;
    }

    public boolean isShowDefault() {
        return mShowDefault;
    }

    public boolean isShowSilent() {
        return mShowSilent;
    }

    public String getValue() {
        return getPersistedString(null);
    }

    public int getRequestCode() {
        return getKey().hashCode() & 0x0000FFFF;
    }

    public void setValue(String value) {
        persistString(value);
        callChangeListener(value);
        notifyChanged();
    }
}