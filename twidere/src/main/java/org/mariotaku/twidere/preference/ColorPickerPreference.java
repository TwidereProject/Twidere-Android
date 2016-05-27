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

package org.mariotaku.twidere.preference;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.preference.iface.IDialogPreference;
import org.mariotaku.twidere.util.TwidereColorUtils;

import me.uucky.colorpicker.ColorPickerDialog;

import static org.mariotaku.twidere.Constants.PRESET_COLORS;
import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

public class ColorPickerPreference extends DialogPreference implements
        IDialogPreference {

    private int mDefaultValue = Color.WHITE;
    private boolean mAlphaSliderEnabled = false;

    public ColorPickerPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setWidgetLayoutResource(R.layout.preference_widget_color_picker);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreferences);
        mAlphaSliderEnabled = a.getBoolean(R.styleable.ColorPickerPreferences_alphaSlider, false);
        setDefaultValue(a.getColor(R.styleable.ColorPickerPreferences_defaultColor, 0));
        a.recycle();
    }

    @Override
    public void setDefaultValue(final Object value) {
        if (!(value instanceof Integer)) return;
        mDefaultValue = (Integer) value;
    }

    @Override
    protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
        if (isPersistent() && defaultValue instanceof Integer) {
            persistInt(restoreValue ? getValue() : (Integer) defaultValue);
        }
    }

    private int getValue() {
        try {
            if (isPersistent()) return getPersistedInt(mDefaultValue);
        } catch (final ClassCastException e) {
            Log.w(LOGTAG, e);
        }
        return mDefaultValue;
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        ColorPickerPreferenceDialogFragment df = ColorPickerPreferenceDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        final ImageView imageView = (ImageView) holder.findViewById(R.id.color);
        imageView.setImageBitmap(TwidereColorUtils.getColorPreviewBitmap(getContext(), getValue(), false));
    }

    public boolean isAlphaSliderEnabled() {
        return mAlphaSliderEnabled;
    }

    public static final class ColorPickerPreferenceDialogFragment extends PreferenceDialogFragmentCompat
            implements DialogInterface.OnShowListener, MaterialDialog.SingleButtonCallback {

        private ColorPickerDialog.Controller mController;


        public static ColorPickerPreferenceDialogFragment newInstance(String key) {
            final ColorPickerPreferenceDialogFragment df = new ColorPickerPreferenceDialogFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final ColorPickerPreference preference = (ColorPickerPreference) getPreference();
            final Context context = getContext();
            final MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
            builder.title(preference.getDialogTitle());
            builder.customView(R.layout.cp__dialog_color_picker, false);

            builder.positiveText(android.R.string.ok);
            builder.negativeText(android.R.string.cancel);
            builder.onPositive(this);
            builder.onNegative(this);
            Dialog dialog = builder.build();
            dialog.setOnShowListener(this);
            return dialog;
        }

        @Override
        public void onDialogClosed(boolean positive) {
            final ColorPickerPreference preference = (ColorPickerPreference) getPreference();
            if (mController == null) return;
            final int color = mController.getColor();
            if (preference.isPersistent()) {
                preference.persistInt(color);
            }
            preference.callChangeListener(color);
            preference.notifyChanged();
        }

        @Override
        public void onShow(DialogInterface dialog) {
            final ColorPickerPreference preference = (ColorPickerPreference) getPreference();
            final Dialog alertDialog = (Dialog) dialog;
            final View windowView = alertDialog.getWindow().getDecorView();
            if (windowView == null) return;
            mController = new ColorPickerDialog.Controller(getContext(), windowView);
            mController.setAlphaEnabled(preference.isAlphaSliderEnabled());
            for (int presetColor : PRESET_COLORS) {
                mController.addColor(ContextCompat.getColor(getContext(), presetColor));
            }
            mController.setInitialColor(preference.getValue());
        }

        @Override
        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
            switch (dialogAction) {
                case POSITIVE:
                    onClick(materialDialog, DialogInterface.BUTTON_POSITIVE);
                    break;
                case NEUTRAL:
                    onClick(materialDialog, DialogInterface.BUTTON_NEUTRAL);
                    break;
                case NEGATIVE:
                    onClick(materialDialog, DialogInterface.BUTTON_NEGATIVE);
                    break;
            }
        }
    }

}
