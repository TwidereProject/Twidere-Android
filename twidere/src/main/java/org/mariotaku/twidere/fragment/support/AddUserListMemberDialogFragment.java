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

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class AddUserListMemberDialogFragment extends BaseSupportDialogFragment implements
        DialogInterface.OnClickListener {

    public static final String FRAGMENT_TAG = "add_user_list_member";
    private AutoCompleteTextView mEditText;
    private UserHashtagAutoCompleteAdapter mUserAutoCompleteAdapter;

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final Bundle args = getArguments();
        if (args == null || !args.containsKey(EXTRA_ACCOUNT_ID) || !args.containsKey(EXTRA_LIST_ID) || !args.containsKey(EXTRA_USERS))
            return;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final String mText = ParseUtils.parseString(mEditText.getText());
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (mText == null || mText.length() <= 0 || twitter == null) return;
                twitter.addUserListMembersAsync(args.getLong(EXTRA_ACCOUNT_ID), args.getLong(EXTRA_LIST_ID));
                break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
        final View view = LayoutInflater.from(wrapped).inflate(R.layout.dialog_auto_complete_textview, null);
        builder.setView(view);
        mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getCharSequence(EXTRA_TEXT));
        }
        mUserAutoCompleteAdapter = new UserHashtagAutoCompleteAdapter(wrapped);
        final Bundle args = getArguments();
        mUserAutoCompleteAdapter.setAccountId(args.getLong(EXTRA_ACCOUNT_ID));
        mEditText.setAdapter(mUserAutoCompleteAdapter);
        mEditText.setThreshold(1);
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        builder.setTitle(R.string.screen_name);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        outState.putCharSequence(EXTRA_TEXT, mEditText.getText());
        super.onSaveInstanceState(outState);
    }

    public static AddUserListMemberDialogFragment show(final FragmentManager fm, final long accountId, final long listId) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ACCOUNT_ID, accountId);
        args.putLong(EXTRA_LIST_ID, listId);
        final AddUserListMemberDialogFragment f = new AddUserListMemberDialogFragment();
        f.setArguments(args);
        f.show(fm, FRAGMENT_TAG);
        return f;
    }

}
