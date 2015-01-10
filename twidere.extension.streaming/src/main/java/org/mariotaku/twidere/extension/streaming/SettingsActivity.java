package org.mariotaku.twidere.extension.streaming;

import org.mariotaku.twidere.Twidere;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity implements Constants, OnSharedPreferenceChangeListener {

	private static final int REQUEST_REQUEST_PERMISSIONS = 101;
	private SharedPreferences mPreferences;

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (PREFERENCE_KEY_ENABLE_STREAMING.equals(key)) {
			final Intent intent = new Intent(this, StreamingService.class);
			if (preferences.getBoolean(key, true)) {
				startService(intent);
			} else {
				stopService(intent);
			}
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_REQUEST_PERMISSIONS: {
				if (resultCode != RESULT_OK) {
					finish();
					return;
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean granted;
		try {
			granted = Twidere.isPermissionGranted(this);
		} catch (final SecurityException e) {
			// TODO show error
			finish();
			return;
		}
		if (!granted) {
			final Intent intent = new Intent(Twidere.INTENT_ACTION_REQUEST_PERMISSIONS);
			intent.setPackage("org.mariotaku.twidere");
			try {
				startActivityForResult(intent, REQUEST_REQUEST_PERMISSIONS);
			} catch (final ActivityNotFoundException e) {

			}
		}
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		addPreferencesFromResource(R.xml.settings);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

}
