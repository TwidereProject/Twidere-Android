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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;

abstract class MultiSelectListPreference extends DialogPreference implements OnMultiChoiceClickListener,
		OnClickListener {

	private final boolean[] mValues, mDefaultValues;
	private SharedPreferences mPreferences;
	private final String[] mNames, mKeys;

	protected MultiSelectListPreference(final Context context) {
		this(context, null);
	}

	protected MultiSelectListPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	protected MultiSelectListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mNames = getNames();
		mKeys = getKeys();
		mDefaultValues = getDefaults();
		final int length = mNames.length;
		if (length != mKeys.length || length != mDefaultValues.length) throw new IllegalArgumentException();
		mValues = new boolean[length];

	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (mPreferences == null) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final SharedPreferences.Editor editor = mPreferences.edit();
				final int length = mKeys.length;
				for (int i = 0; i < length; i++) {
					editor.putBoolean(mKeys[i], mValues[i]);
				}
				editor.commit();
				break;
		}

	}

	@Override
	public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
		mValues[which] = isChecked;
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		mPreferences = getDefaultSharedPreferences();
		if (mPreferences == null) return;
		final int length = mKeys.length;
		for (int i = 0; i < length; i++) {
			mValues[i] = mPreferences.getBoolean(mKeys[i], mDefaultValues[i]);
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setMultiChoiceItems(mNames, mValues, this);
	}

	protected abstract boolean[] getDefaults();

	protected SharedPreferences getDefaultSharedPreferences() {
		return getSharedPreferences();
	}

	protected abstract String[] getKeys();

	protected abstract String[] getNames();

}
