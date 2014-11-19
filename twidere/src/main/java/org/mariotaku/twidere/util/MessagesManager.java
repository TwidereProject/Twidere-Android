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

package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.mariotaku.twidere.Constants;

public final class MessagesManager implements Constants {

    private final Context mContext;
    private final SharedPreferences mPreferences;

    public MessagesManager(final Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean addMessageCallback(final Activity activity) {
        if (activity == null) return false;
        return true;
    }

    public boolean removeMessageCallback(final Activity activity) {
        if (activity == null) return false;
        return true;
    }

    public void showErrorMessage(final CharSequence message, final boolean long_message) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showErrorMessage(best, message, long_message);
            return;
        }
        if (showToast()) {
            Utils.showErrorMessage(mContext, message, long_message);
            return;
        }
    }

    public void showErrorMessage(final int actionRes, final Exception e, final boolean long_message) {
        final String action = mContext.getString(actionRes);
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showErrorMessage(best, action, e, long_message);
            return;
        }
        if (showToast()) {
            Utils.showErrorMessage(mContext, action, e, long_message);
            return;
        }
    }

    public void showErrorMessage(final int action_res, final String message, final boolean longMessage) {
        final String action = mContext.getString(action_res);
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showErrorMessage(best, action, message, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showErrorMessage(mContext, action, message, longMessage);
            return;
        }
    }

    public void showInfoMessage(final CharSequence message, final boolean longMessage) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showInfoMessage(best, message, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showInfoMessage(mContext, message, longMessage);
            return;
        }
    }

    public void showInfoMessage(final int messageRes, final boolean longMessage) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showInfoMessage(best, messageRes, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showInfoMessage(mContext, messageRes, longMessage);
            return;
        }
    }

    public void showOkMessage(final CharSequence message, final boolean longMessage) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showOkMessage(best, message, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showOkMessage(mContext, message, longMessage);
        }
    }

    public void showOkMessage(final int messageRes, final boolean longMessage) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showOkMessage(best, messageRes, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showOkMessage(mContext, messageRes, longMessage);
            return;
        }
    }

    public void showWarnMessage(final int messageRes, final boolean longMessage) {
        final Activity best = getBestActivity();
        if (best != null) {
            Utils.showWarnMessage(best, messageRes, longMessage);
            return;
        }
        if (showToast()) {
            Utils.showWarnMessage(mContext, messageRes, longMessage);
        }
    }

    private Activity getBestActivity() {
        return null;
    }

    private boolean showToast() {
        return true;
    }

}
