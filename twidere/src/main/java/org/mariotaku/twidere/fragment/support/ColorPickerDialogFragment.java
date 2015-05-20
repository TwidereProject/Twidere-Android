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
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IDialogFragmentCallback;

import me.uucky.colorpicker.ColorPickerDialog;

public final class ColorPickerDialogFragment extends BaseSupportDialogFragment implements
        DialogInterface.OnClickListener {

    private ColorPickerDialog.Controller mController;

    @Override
    public void onCancel(final DialogInterface dialog) {
        super.onCancel(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onCancelled();
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final FragmentActivity a = getActivity();
        if (!(a instanceof Callback) || mController == null) return;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final int color = mController.getColor();
                ((Callback) a).onColorSelected(color);
                break;
            }
            case DialogInterface.BUTTON_NEUTRAL: {
                ((Callback) a).onColorCleared();
                break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final int color;
        final Bundle args = getArguments();
        if (savedInstanceState != null) {
            color = savedInstanceState.getInt(EXTRA_COLOR, Color.WHITE);
        } else {
            color = args.getInt(EXTRA_COLOR, Color.WHITE);
        }

        final FragmentActivity activity = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(me.uucky.colorpicker.R.layout.cp__dialog_color_picker);
        builder.setPositiveButton(android.R.string.ok, this);
        if (args.getBoolean(EXTRA_CLEAR_BUTTON, false)) {
            builder.setNeutralButton(R.string.clear, this);
        }
        builder.setNegativeButton(android.R.string.cancel, this);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface di) {
                final Dialog dialog = (Dialog) di;
                mController = new ColorPickerDialog.Controller(dialog.getContext(), dialog.getWindow().getDecorView());

                final boolean showAlphaSlider = args.getBoolean(EXTRA_ALPHA_SLIDER, true);
                final Resources res = getResources();
                for (int presetColor : PRESET_COLORS) {
                    mController.addColor(res.getColor(presetColor));
                }
                mController.setAlphaEnabled(showAlphaSlider);
                mController.setInitialColor(color);
            }
        });
        return dialog;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final FragmentActivity a = getActivity();
        if (a instanceof Callback) {
            ((Callback) a).onDismissed();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        if (mController != null) {
            outState.putInt(EXTRA_COLOR, mController.getColor());
        }
        super.onSaveInstanceState(outState);
    }

    public interface Callback extends IDialogFragmentCallback {

        void onColorCleared();

        void onColorSelected(int color);

    }

}
