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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class CreateUserListDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	private EditText mEditName, mEditDescription;
	private CheckBox mPublicCheckBox;
	private String mName, mDescription;
	private long mAccountId;
	private int mListId;
	private boolean mIsPublic = true;
	private AsyncTwitterWrapper mTwitterWrapper;

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (mAccountId <= 0) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				mName = ParseUtils.parseString(mEditName.getText());
				mDescription = ParseUtils.parseString(mEditDescription.getText());
				mIsPublic = mPublicCheckBox.isChecked();
				if (mName == null || mName.length() <= 0) return;
				mTwitterWrapper.createUserListAsync(mAccountId, mName, mIsPublic, mDescription);
				break;
			}
		}

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		mTwitterWrapper = getApplication().getTwitterWrapper();
		final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
		mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		final View view = LayoutInflater.from(wrapped).inflate(R.layout.edit_user_list_detail, null);
		builder.setView(view);
		mEditName = (EditText) view.findViewById(R.id.name);
		mEditDescription = (EditText) view.findViewById(R.id.description);
		mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
		if (mName != null) {
			mEditName.setText(mName);
		}
		if (mDescription != null) {
			mEditDescription.setText(mDescription);
		}
		mPublicCheckBox.setChecked(mIsPublic);
		builder.setTitle(R.string.new_user_list);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
		outState.putInt(EXTRA_LIST_ID, mListId);
		outState.putString(EXTRA_LIST_NAME, mName);
		outState.putString(EXTRA_DESCRIPTION, mDescription);
		outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
		super.onSaveInstanceState(outState);
	}

}
