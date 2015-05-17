/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.preference;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;

import org.mariotaku.twidere.activity.AssistLauncherActivity;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 15/3/30.
 */
public class ComposeNowPreference extends ComponentStatePreference {
    public ComposeNowPreference(Context context) {
        super(context);
    }

    public ComposeNowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComposeNowPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean isComponentAvailable() {
        return Utils.isComposeNowSupported(getContext());
    }

    @Override
    protected ComponentName getComponentName(Context context, AttributeSet attrs) {
        return new ComponentName(context, AssistLauncherActivity.class);
    }
}
