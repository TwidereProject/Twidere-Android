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

import org.acra.ACRA;
import org.mariotaku.twidere.activity.TwitterLinkHandlerActivity;

/**
 * Created by mariotaku on 15/5/25.
 */
public class ErrorLogger {

    private static boolean sEnabled;

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static void exception(final Throwable t) {
        if (!sEnabled) return;
        ACRA.getErrorReporter().handleSilentException(t);
    }

}
