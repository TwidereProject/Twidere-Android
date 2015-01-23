package org.mariotaku.twidere.extension.twitlonger;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AboutActivity extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.about);
	}

}
