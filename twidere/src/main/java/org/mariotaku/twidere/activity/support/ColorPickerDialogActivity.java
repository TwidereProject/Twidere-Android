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

package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import org.mariotaku.twidere.fragment.support.ColorPickerDialogFragment;
import org.mariotaku.twidere.fragment.support.ColorPickerDialogFragment.Callback;
import org.mariotaku.twidere.util.ThemeUtils;

public class ColorPickerDialogActivity extends BaseSupportDialogActivity implements Callback {

	public static final int RESULT_CLEARED = -2;

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getNoDisplayThemeResource(this);
	}

	@Override
	public void onCancelled() {
		finish();
	}

	@Override
	public void onColorCleared() {
		setResult(RESULT_CLEARED);
		finish();
	}

	@Override
	public void onColorSelected(final int color) {
		final Intent intent = new Intent();
		intent.putExtra(EXTRA_COLOR, color);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onDismissed() {
		finish();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			final Intent intent = getIntent();
			final ColorPickerDialogFragment f = new ColorPickerDialogFragment();
			final Bundle args = new Bundle();
			args.putInt(EXTRA_COLOR, intent.getIntExtra(EXTRA_COLOR, Color.WHITE));
			args.putBoolean(EXTRA_CLEAR_BUTTON, intent.getBooleanExtra(EXTRA_CLEAR_BUTTON, false));
			args.putBoolean(EXTRA_ALPHA_SLIDER, intent.getBooleanExtra(EXTRA_ALPHA_SLIDER, true));
			f.setArguments(args);
			f.show(getSupportFragmentManager(), "color_picker_dialog");
		}
	}

}
