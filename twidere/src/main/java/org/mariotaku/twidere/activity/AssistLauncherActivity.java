package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;

public class AssistLauncherActivity extends Activity implements Constants {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		final String composeNowAction = prefs.getString(KEY_COMPOSE_NOW_ACTION, VALUE_COMPOSE_NOW_ACTION_COMPOSE), action;
		if (VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO.equals(composeNowAction)) {
			action = INTENT_ACTION_COMPOSE_TAKE_PHOTO;
		} else if (VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE.equals(composeNowAction)) {
			action = INTENT_ACTION_COMPOSE_PICK_IMAGE;
		} else {
			action = INTENT_ACTION_COMPOSE;
		}
		final Intent intent = new Intent(action);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.setClass(this, ComposeActivity.class);
		startActivity(intent);
		finish();
	}

}
