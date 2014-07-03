package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class AutoFixListPreference extends ListPreference {

	public AutoFixListPreference(final Context context) {
		super(context);
	}

	public AutoFixListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
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
