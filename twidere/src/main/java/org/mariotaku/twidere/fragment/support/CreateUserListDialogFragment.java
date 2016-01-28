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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.CheckBox;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.text.validator.UserListNameValidator;
import org.mariotaku.twidere.util.ParseUtils;

public class CreateUserListDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final AlertDialog alertDialog = (AlertDialog) dialog;
                final Bundle args = getArguments();
                final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
                final MaterialEditText mEditName = (MaterialEditText) alertDialog.findViewById(R.id.name);
                final MaterialEditText mEditDescription = (MaterialEditText) alertDialog.findViewById(R.id.description);
                final CheckBox mPublicCheckBox = (CheckBox) alertDialog.findViewById(R.id.is_public);
                final String name = ParseUtils.parseString(mEditName.getText());
                final String description = ParseUtils.parseString(mEditDescription.getText());
                final boolean isPublic = mPublicCheckBox.isChecked();
                if (TextUtils.isEmpty(name)) return;
                mTwitterWrapper.createUserListAsync(accountId, name, isPublic, description);
                break;
            }
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.dialog_user_list_detail_editor);

        builder.setTitle(R.string.new_user_list);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, this);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final AlertDialog alertDialog = (AlertDialog) dialog;
                MaterialEditText editName = (MaterialEditText) alertDialog.findViewById(R.id.name);
                MaterialEditText editDescription = (MaterialEditText) alertDialog.findViewById(R.id.description);
                CheckBox publicCheckBox = (CheckBox) alertDialog.findViewById(R.id.is_public);
                editName.addValidator(new UserListNameValidator(getString(R.string.invalid_list_name)));
            }
        });
        return dialog;
    }

}
