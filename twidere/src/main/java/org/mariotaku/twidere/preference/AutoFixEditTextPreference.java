package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class AutoFixEditTextPreference extends EditTextPreference {

	public AutoFixEditTextPreference(final Context context) {
		super(context);
	}

	public AutoFixEditTextPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoFixEditTextPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		try {
			super.onSetInitialValue(restoreValue, defaultValue);
		} catch (final ClassCastException e) {
			final SharedPreferences prefs = getSharedPreferences();
			if (prefs != null) {
				prefs.edit().remove(getKey()).apply();
			}
		}
	}

}
