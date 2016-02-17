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
import android.support.annotation.NonNull;

/**
 * Created by mariotaku on 15/7/8.
 */
public abstract class BugReporter {

    private static BugReporter sImplementation;

    public static void setImplementation(BugReporter impl) {
        sImplementation = impl;
    }

    public static void init(Application application) {
        if (sImplementation == null) return;
        sImplementation.initImpl(application);
    }

    public static void log(int priority, String tag, String msg) {
        if (sImplementation == null) return;
        sImplementation.logImpl(priority, tag, msg);
    }

    public static void logException(@NonNull Throwable throwable) {
        if (sImplementation == null) return;
        sImplementation.logExceptionImpl(throwable);
    }

    protected abstract void logImpl(int priority, String tag, String msg);

    protected abstract void logExceptionImpl(@NonNull Throwable throwable);

    protected abstract void initImpl(Application application);
}
