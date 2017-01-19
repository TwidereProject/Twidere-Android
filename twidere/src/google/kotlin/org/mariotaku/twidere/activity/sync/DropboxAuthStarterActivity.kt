package org.mariotaku.twidere.activity.sync

import android.os.Bundle
import com.dropbox.core.android.Auth
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.Constants.DROPBOX_APP_KEY
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.model.sync.DropboxSyncProviderInfo

/**
 * Created by mariotaku on 2016/12/7.
 */
class DropboxAuthStarterActivity : BaseActivity() {

    private var shouldGetAuthResult: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Auth.startOAuth2Authentication(this, DROPBOX_APP_KEY)
    }

    override fun onResume() {
        super.onResume()
        if (shouldGetAuthResult) {
            val oauthToken = Auth.getOAuth2Token()
            if (oauthToken != null) {
                preferences[dataSyncProviderInfoKey] = DropboxSyncProviderInfo(oauthToken)
            }
            finish()
            shouldGetAuthResult = false
        }
    }

    override fun onPause() {
        super.onPause()
        shouldGetAuthResult = true
    }
}