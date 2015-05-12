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

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.TwidereColorUtils;

import me.uucky.colorpicker.ColorPickerDialog;

public class ColorPickerPreference extends DialogPreference implements DialogInterface.OnClickListener, Constants {

    private int mDefaultValue = Color.WHITE;
    private boolean mAlphaSliderEnabled = false;

    private ColorPickerDialog.Controller mController;

    public ColorPickerPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
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
    protected void onBindView(@NonNull final View view) {
        super.onBindView(view);
        final ImageView imageView = (ImageView) view.findViewById(R.id.color);
        imageView.setImageBitmap(TwidereColorUtils.getColorPreviewBitmap(getContext(), getValue(), false));
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

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        mController = ColorPickerDialog.Controller.applyToDialogBuilder(builder);
        mController.setAlphaEnabled(mAlphaSliderEnabled);
        final Resources res = builder.getContext().getResources();
        for (int presetColor : PRESET_COLORS) {
            mController.addColor(res.getColor(presetColor));
        }
        mController.setInitialColor(getValue());
        builder.setPositiveButton(res.getString(android.R.string.ok), this);
        builder.setNegativeButton(res.getString(android.R.string.cancel), this);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        final Dialog dialog = getDialog();
        if (dialog != null && mController != null) {
            dialog.setOnShowListener(mController);
        }
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mController == null) return;
                final int color = mController.getColor();
                if (isPersistent()) {
                    persistInt(color);
                }
                final OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
                if (listener != null) {
                    listener.onPreferenceChange(this, color);
                }
                break;
        }
    }

    private int getValue() {
        try {
            if (isPersistent()) return getPersistedInt(mDefaultValue);
        } catch (final ClassCastException e) {
            e.printStackTrace();
        }
        return mDefaultValue;
    }

}
