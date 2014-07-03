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
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ThemeUtils;

public class DestroyUserListDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "destroy_user_list";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final ParcelableUserList user_list = getUserList();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (user_list == null || twitter == null) return;
				twitter.destroyUserListAsync(user_list.account_id, user_list.id);
				break;
			default:
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		final ParcelableUserList userList = getUserList();
		if (userList != null) {
			builder.setTitle(getString(R.string.delete_user_list, userList.name));
			builder.setMessage(getString(R.string.delete_user_list_confirm_message, userList.name));
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

	private ParcelableUserList getUserList() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_USER_LIST)) return null;
		return args.getParcelable(EXTRA_USER_LIST);
	}

	public static DestroyUserListDialogFragment show(final FragmentManager fm, final ParcelableUserList user_list) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_USER_LIST, user_list);
		final DestroyUserListDialogFragment f = new DestroyUserListDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}
