/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.activity.support;

import android.annotation.SuppressLint;
import android.os.Bundle;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ThemeUtils;

@SuppressLint("Registered")
public class BaseSupportDialogActivity extends ThemedFragmentActivity implements Constants {

    private boolean mInstanceStateSaved;

    @Override
    public int getThemeColor() {
        return ThemeUtils.getThemeColor(this, getThemeResourceId());
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getDialogThemeResource(this);
    }

    public TwidereApplication getTwidereApplication() {
        return (TwidereApplication) getApplication();
    }

    protected boolean isStateSaved() {
        return mInstanceStateSaved;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstanceStateSaved = false;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mInstanceStateSaved = true;
        super.onSaveInstanceState(outState);
    }
}
