package org.mariotaku.twidere.activity

import android.os.Bundle
import com.dropbox.core.android.Auth
import org.mariotaku.twidere.Constants.DROPBOX_APP_KEY
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DROPBOX_ACCESS_TOKEN

/**
 * Created by mariotaku on 2016/12/7.
 */
class DropboxAuthStarterActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Auth.startOAuth2Authentication(this, DROPBOX_APP_KEY)

    }

    override fun onResume() {
        super.onResume()
        val oauthToken = Auth.getOAuth2Token()
        if (oauthToken != null) {
            preferences.edit().putString(KEY_DROPBOX_ACCESS_TOKEN, oauthToken).apply()
        }
        finish()
    }
}