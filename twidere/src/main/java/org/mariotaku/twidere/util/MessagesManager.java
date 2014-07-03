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
import org.mariotaku.twidere.activity.support.BaseSupportActivity;
import org.mariotaku.twidere.activity.support.HomeActivity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MessagesManager implements Constants {

	private final Set<Activity> mMessageCallbacks = Collections.synchronizedSet(new HashSet<Activity>());
	private final Context mContext;
	private final SharedPreferences mPreferences;

	public MessagesManager(final Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public boolean addMessageCallback(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.add(activity);
	}

	public boolean removeMessageCallback(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.remove(activity);
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

	public void showErrorMessage(final int action_res, final String message, final boolean long_message) {
		final String action = mContext.getString(action_res);
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showErrorMessage(best, action, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showErrorMessage(mContext, action, message, long_message);
			return;
		}
	}

	public void showInfoMessage(final CharSequence message, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showInfoMessage(best, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showInfoMessage(mContext, message, long_message);
			return;
		}
	}

	public void showInfoMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showInfoMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showInfoMessage(mContext, message_res, long_message);
			return;
		}
	}

	public void showOkMessage(final CharSequence message, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showOkMessage(best, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showOkMessage(mContext, message, long_message);
		}
	}

	public void showOkMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showOkMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showOkMessage(mContext, message_res, long_message);
			return;
		}
	}

	public void showWarnMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showWarnMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showWarnMessage(mContext, message_res, long_message);
		}
	}

	private Activity getBestActivity() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof BaseSupportActivity) {
				final BaseSupportActivity base = (BaseSupportActivity) activity;
				if (base.isOnTop()) return base;
			}
		}
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) {
				final HomeActivity home = (HomeActivity) activity;
				if (home.isVisible()) return home;
			}
		}
		for (final Activity activity : mMessageCallbacks)
			if (ThemeUtils.isFloatingWindow(activity)) return activity;
		return null;
	}

	private boolean showToast() {
		return mPreferences.getBoolean(KEY_BACKGROUND_TOAST_NOTIFICATION, false);
	}

}
