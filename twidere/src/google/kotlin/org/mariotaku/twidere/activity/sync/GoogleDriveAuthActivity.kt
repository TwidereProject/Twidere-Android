package org.mariotaku.twidere.activity.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.AccountPicker
import com.google.api.services.drive.DriveScopes
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.set
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.constant.dataSyncProviderInfoKey
import org.mariotaku.twidere.model.sync.GoogleDriveSyncProviderInfo


class GoogleDriveAuthActivity : BaseActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = AccountPicker.newChooseAccountIntent(null, null,
                arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), false, null, null, null, null)
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_ACCOUNT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CHOOSE_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    val type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                    val account = Account(name, type)
                    task {
                        return@task GoogleAuthUtil.getToken(this, account, "oauth2:${DriveScopes.DRIVE_APPDATA}")
                    }.successUi { accessToken ->
                        preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(accessToken)
                        finish()
                    }.fail { ex ->
                        if (ex is UserRecoverableAuthException) {
                            startActivityForResult(ex.intent, REQUEST_CODE_AUTH_ERROR_RECOVER)
                        } else {
                            finish()
                        }
                    }
                }
            }
            REQUEST_CODE_AUTH_ERROR_RECOVER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    val type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                    val token = data.getStringExtra(AccountManager.KEY_AUTHTOKEN)
                    preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(token)
                }
                finish()
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_CHOOSE_ACCOUNT: Int = 101
        private const val REQUEST_CODE_AUTH_ERROR_RECOVER: Int = 102
    }
}