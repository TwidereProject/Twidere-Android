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

package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by mariotaku on 14-6-24.
 */
public class SupportMessageDialogFragment extends DialogFragment {
    private static final String EXTRA_MESSAGE = "message";

    public static SupportMessageDialogFragment show(FragmentActivity activity, String message, String tag) {
        SupportMessageDialogFragment df = new SupportMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_MESSAGE, message);
        df.setArguments(args);
        df.show(activity.getSupportFragmentManager(), tag);
        return df;
    }

    public static SupportMessageDialogFragment create(String message) {
        SupportMessageDialogFragment df = new SupportMessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_MESSAGE, message);
        df.setArguments(args);
        return df;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final Bundle args = getArguments();
        builder.setMessage(args.getString(EXTRA_MESSAGE));
        builder.setPositiveButton(android.R.string.ok, null);
        return builder.create();
    }
}
