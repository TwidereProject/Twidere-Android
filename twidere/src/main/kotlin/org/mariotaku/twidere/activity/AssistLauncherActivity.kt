package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*

class AssistLauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val action = when (prefs.getString(KEY_COMPOSE_NOW_ACTION, VALUE_COMPOSE_NOW_ACTION_COMPOSE)) {
            VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO -> INTENT_ACTION_COMPOSE_TAKE_PHOTO
            VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE -> INTENT_ACTION_COMPOSE_PICK_IMAGE
            else -> INTENT_ACTION_COMPOSE
        }
        val intent = Intent(action)
        intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        intent.setClass(this, ComposeActivity::class.java)
        startActivity(intent)
        finish()
    }

}
