package org.mariotaku.twidere.service

import android.accounts.*
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.mariotaku.ktextension.set
import org.mariotaku.ktextension.weak
import org.mariotaku.twidere.activity.SignInActivity
import org.mariotaku.twidere.extension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.twidere.util.notification.NotificationChannelsManager


class AccountAuthenticatorService : Service() {

    private val authenticator by lazy { TwidereAccountAuthenticator(this) }

    override fun onCreate() {
        super.onCreate()
        val weakThis by weak(this)
        AccountManager.get(this).addOnAccountsUpdatedListenerSafe(OnAccountsUpdateListener {
            val service = weakThis ?: return@OnAccountsUpdateListener
            NotificationChannelsManager.updateAccountChannelsAndGroups(service)
        }, updateImmediately = true)
    }

    override fun onBind(intent: Intent): IBinder? {
        if (intent.action != AccountManager.ACTION_AUTHENTICATOR_INTENT) return null
        return authenticator.iBinder
    }

    internal class TwidereAccountAuthenticator(val context: Context) : AbstractAccountAuthenticator(context) {

        override fun addAccount(response: AccountAuthenticatorResponse, accountType: String,
                authTokenType: String?, requiredFeatures: Array<String>?,
                options: Bundle?): Bundle {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            val result = Bundle()
            result[AccountManager.KEY_INTENT] = intent
            return result
        }

        override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle?): Bundle {
            val am = AccountManager.get(context)
            val authToken = am.peekAuthToken(account, authTokenType)
            if (authToken.isNullOrEmpty()) {
                val intent = Intent(context, SignInActivity::class.java)
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                val result = Bundle()
                result[AccountManager.KEY_INTENT] = intent
                return result
            }
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        override fun getAuthTokenLabel(authTokenType: String): String {
            return authTokenType
        }

        override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account,
                options: Bundle?) = null

        override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = null

        override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account,
                features: Array<String>) = null

        override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account,
                authTokenType: String, options: Bundle?) = null
    }

}