/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;

/**
 * Created by mariotaku on 15/7/8.
 */
public class TwidereBugReporter extends BugReporter implements Constants {

    @Override
    protected void logImpl(@Nullable String message, @Nullable Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, message, throwable);
        }
    }

    @Override
    protected void errorImpl(@Nullable String message, @Nullable Throwable throwable) {
        if (throwable == null && message == null) {
            throw new NullPointerException("Message and Throwable can't be both null");
        }
        if (message != null) {
            if (BuildConfig.DEBUG) {
                Log.w(LOGTAG, message, throwable);
            }
            return;
        }
        if (BuildConfig.DEBUG) {
            Log.w(LOGTAG, throwable);
        }
    }

    @Override
    protected void initImpl(final Application application) {
    }

}
