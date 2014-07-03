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

package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ThemeUtils;

public class DestroySavedSearchDialogFragment extends BaseSupportDialogFragment implements
		DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "destroy_saved_search";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final long accountId = getAccountId();
				final int searchId = getSearchId();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (searchId <= 0 || twitter == null) return;
				twitter.destroySavedSearchAsync(accountId, searchId);
				break;
			default:
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		final String name = getSearchName();
		if (name != null) {
			builder.setTitle(getString(R.string.destroy_saved_search, name));
			builder.setMessage(getString(R.string.destroy_saved_search_confirm_message, name));
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

	private long getAccountId() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_ACCOUNT_ID)) return -1;
		return args.getLong(EXTRA_ACCOUNT_ID);
	}

	private int getSearchId() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_SEARCH_ID)) return -1;
		return args.getInt(EXTRA_SEARCH_ID);
	}

	private String getSearchName() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_NAME)) return null;
		return args.getString(EXTRA_NAME);
	}

	public static DestroySavedSearchDialogFragment show(final FragmentManager fm, final long accountId,
			final int searchId, final String name) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ACCOUNT_ID, accountId);
		args.putInt(EXTRA_SEARCH_ID, searchId);
		args.putString(EXTRA_NAME, name);
		final DestroySavedSearchDialogFragment f = new DestroySavedSearchDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}
