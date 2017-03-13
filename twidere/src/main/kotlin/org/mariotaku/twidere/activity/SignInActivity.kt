/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArraySet
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.bluelinelabs.logansquare.LoganSquare
import kotlinx.android.synthetic.main.activity_sign_in.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterOAuth
import org.mariotaku.microblog.library.twitter.auth.BasicAuthorization
import org.mariotaku.microblog.library.twitter.auth.EmptyAuthorization
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.oauth.OAuthAuthorization
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_CREDENTIALS_TYPE
import org.mariotaku.twidere.constant.chromeCustomTabKey
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.constant.randomizeAccountNameKey
import org.mariotaku.twidere.extension.applyTheme
import org.mariotaku.twidere.extension.getNonEmptyString
import org.mariotaku.twidere.extension.model.getColor
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.extension.model.official
import org.mariotaku.twidere.fragment.APIEditorDialogFragment
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.BasicCredentials
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.account.cred.EmptyCredentials
import org.mariotaku.twidere.model.account.cred.OAuthCredentials
import org.mariotaku.twidere.model.analyzer.SignIn
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*


class SignInActivity : BaseActivity(), OnClickListener, TextWatcher, APIEditorDialogFragment.APIEditorCallback {
    private lateinit var apiConfig: CustomAPIConfig
    private var apiChangeTimestamp: Long = 0
    private var signInTask: AbstractSignInTask? = null

    private var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    private var accountAuthenticatorResult: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountAuthenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()

        setContentView(R.layout.activity_sign_in)

        if (savedInstanceState != null) {
            apiConfig = savedInstanceState.getParcelable(EXTRA_API_CONFIG)
            apiChangeTimestamp = savedInstanceState.getLong(EXTRA_API_LAST_CHANGE)
        } else {
            apiConfig = kPreferences[defaultAPIConfigKey]
        }

        val isTwipOMode = apiConfig.credentialsType == Credentials.Type.EMPTY
        usernamePasswordContainer.visibility = if (isTwipOMode) View.GONE else View.VISIBLE

        editUsername.addTextChangedListener(this)
        editPassword.addTextChangedListener(this)

        signIn.setOnClickListener(this)
        signUp.setOnClickListener(this)
        passwordSignIn.setOnClickListener(this)

        val color = ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.material_light_green))
        ViewCompat.setBackgroundTintList(signIn, color)

        updateSignInType()
        setSignInButton()

        if (savedInstanceState == null) {
            // Only start at the first time
            updateDefaultFeatures()
        }
    }

    override fun onDestroy() {
        loaderManager.destroyLoader(0)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_sign_in, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_API -> {
                if (resultCode == Activity.RESULT_OK) {
                    apiConfig = data!!.getParcelableExtra(EXTRA_API_CONFIG)
                    updateSignInType()
                }
                setSignInButton()
                invalidateOptionsMenu()
            }
            REQUEST_BROWSER_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    handleBrowserLoginResult(data)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun finish() {
        accountAuthenticatorResponse?.let { response ->
            // send the result bundle back if set, otherwise send an error.
            if (accountAuthenticatorResult != null) {
                response.onResult(accountAuthenticatorResult)
            } else {
                response.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            }
            accountAuthenticatorResponse = null
        }
        super.finish()
    }

    override fun afterTextChanged(s: Editable) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onClick(v: View) {
        when (v) {
            signUp -> {
                val signUpUrl = apiConfig.signUpUrl ?: return
                val uri = Uri.parse(signUpUrl)
                if (preferences[chromeCustomTabKey]) {
                    val builder = CustomTabsIntent.Builder()
                    builder.setToolbarColor(overrideTheme.colorToolbar)
                    val intent = builder.build()
                    try {
                        intent.launchUrl(this, uri)
                    } catch (e: ActivityNotFoundException) {
                        // Ignore
                    }
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        // Ignore
                    }
                }
            }
            signIn -> {
                if (usernamePasswordContainer.visibility != View.VISIBLE) {
                    editUsername.text = null
                    editPassword.text = null
                }
                if (apiConfig.credentialsType == Credentials.Type.OAUTH) {
                    doBrowserLogin()
                } else {
                    doLogin()
                }
            }
            passwordSignIn -> {
                executeAfterFragmentResumed { fragment ->
                    val df = PasswordSignInDialogFragment()
                    df.show(fragment.supportFragmentManager, "password_sign_in")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val accountKeys = DataStoreUtils.getActivatedAccountKeys(this)
                if (accountKeys.isNotEmpty()) {
                    onBackPressed()
                }
            }
            R.id.settings -> {
                if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
                    return false
                startActivity(IntentUtils.settings())
            }
            R.id.edit_api -> {
                if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
                    return false
                setDefaultAPI()
                val df = APIEditorDialogFragment()
                df.arguments = Bundle {
                    this[EXTRA_API_CONFIG] = apiConfig
                    this[APIEditorDialogFragment.EXTRA_SHOW_LOAD_DEFAULTS] = true
                }
                df.show(supportFragmentManager, "edit_api_config")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val itemBrowser = menu.findItem(R.id.open_in_browser)
        if (itemBrowser != null) {
            val is_oauth = apiConfig.credentialsType == Credentials.Type.OAUTH
            itemBrowser.isVisible = is_oauth
            itemBrowser.isEnabled = is_oauth
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(EXTRA_API_CONFIG, apiConfig)
        outState.putLong(EXTRA_API_LAST_CHANGE, apiChangeTimestamp)
        super.onSaveInstanceState(outState)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        setSignInButton()
    }

    override fun onSaveAPIConfig(config: CustomAPIConfig) {
        apiConfig = config

        updateSignInType()
        setSignInButton()
        invalidateOptionsMenu()
    }

    internal fun updateSignInType() {
        when (apiConfig.credentialsType) {
            Credentials.Type.XAUTH, Credentials.Type.BASIC -> {
                usernamePasswordContainer.visibility = View.VISIBLE
            }
            Credentials.Type.EMPTY -> {
                usernamePasswordContainer.visibility = View.GONE
            }
            else -> {
                usernamePasswordContainer.visibility = View.GONE
            }
        }
    }

    internal fun doBrowserLogin(): Boolean {
        if (apiConfig.credentialsType != Credentials.Type.OAUTH || signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
            return true
        val intent = Intent(this, BrowserSignInActivity::class.java)
        intent.putExtra(EXTRA_API_CONFIG, apiConfig)
        startActivityForResult(intent, REQUEST_BROWSER_SIGN_IN)
        return false
    }

    internal fun doLogin() {
        if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING) {
            signInTask!!.cancel(true)
        }

        val username = editUsername.text.toString()
        val password = editPassword.text.toString()
        signInTask = SignInTask(this, username, password, apiConfig)
        AsyncTaskUtils.executeTask<AbstractSignInTask, Any>(signInTask)
    }

    internal fun onSignInResult(result: SignInResponse) {
        val am = AccountManager.get(this)
        setSignInButton()
        if (result.alreadyLoggedIn) {
            result.updateAccount(am)
            deleteAccountData(contentResolver, result.user.key)
            Toast.makeText(this, R.string.message_toast_already_logged_in, Toast.LENGTH_SHORT).show()
        } else {
            result.addAccount(am, preferences[randomizeAccountNameKey])
            Analyzer.log(SignIn(true, accountType = result.accountType.first, credentialsType = apiConfig.credentialsType,
                    officialKey = result.accountType.second?.official ?: false))
            finishSignIn()
        }
    }

    internal fun dismissDialogFragment(tag: String) {
        executeAfterFragmentResumed {
            val fm = supportFragmentManager
            val f = fm.findFragmentByTag(tag)
            if (f is DialogFragment) {
                f.dismiss()
            }
            Unit
        }
    }


    internal fun onSignInError(exception: Exception) {
        DebugLog.w(LOGTAG, "Sign in error", exception)
        var errorReason: String? = null
        if (exception is AuthenticityTokenException) {
            Toast.makeText(this, R.string.message_toast_wrong_api_key, Toast.LENGTH_SHORT).show()
            errorReason = "wrong_api_key"
        } else if (exception is WrongUserPassException) {
            Toast.makeText(this, R.string.message_toast_wrong_username_password, Toast.LENGTH_SHORT).show()
            errorReason = "wrong_username_password"
        } else if (exception is SignInTask.WrongBasicCredentialException) {
            Toast.makeText(this, R.string.message_toast_wrong_username_password, Toast.LENGTH_SHORT).show()
            errorReason = "wrong_username_password"
        } else if (exception is SignInTask.WrongAPIURLFormatException) {
            Toast.makeText(this, R.string.message_toast_wrong_api_key, Toast.LENGTH_SHORT).show()
            errorReason = "wrong_api_key"
        } else if (exception is LoginVerificationException) {
            Toast.makeText(this, R.string.message_toast_login_verification_failed, Toast.LENGTH_SHORT).show()
            errorReason = "login_verification_failed"
        } else if (exception is AuthenticationException) {
            Utils.showErrorMessage(this, getString(R.string.action_signing_in), exception.cause, true)
        } else {
            Utils.showErrorMessage(this, getString(R.string.action_signing_in), exception, true)
        }
        Analyzer.log(SignIn(false, credentialsType = apiConfig.credentialsType,
                errorReason = errorReason, accountType = apiConfig.type))
    }

    internal fun onSignInStart() {
        showSignInProgressDialog()
    }

    internal fun showSignInProgressDialog() {
        executeAfterFragmentResumed {
            if (isFinishing) return@executeAfterFragmentResumed
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            val fragment = ProgressDialogFragment()
            fragment.isCancelable = false
            fragment.show(ft, FRAGMENT_TAG_SIGN_IN_PROGRESS)
        }
    }

    internal fun setUsernamePassword(username: String, password: String) {
        editUsername.setText(username)
        editPassword.setText(password)
    }

    private fun updateDefaultFeatures() {
        val weakThis = WeakReference(this)
        ProgressDialogFragment.show(supportFragmentManager, FRAGMENT_TAG_LOADING_DEFAULT_FEATURES)
        task {
            val activity = weakThis.get() ?: return@task
            if (activity.isFinishing) return@task
            activity.defaultFeatures.loadRemoteSettings(activity.restHttpClient)
        }.successUi {
            val activity = weakThis.get() ?: return@successUi
            if (activity.isFinishing) return@successUi
            val apiConfig = activity.apiConfig
            val defaultFeatures = activity.defaultFeatures
            val preferences = activity.preferences
            if (apiConfig.consumerKey == TWITTER_CONSUMER_KEY && apiConfig.consumerSecret == TWITTER_CONSUMER_SECRET) {
                apiConfig.consumerKey = defaultFeatures.defaultTwitterConsumerKey ?: TWITTER_CONSUMER_KEY
                apiConfig.consumerSecret = defaultFeatures.defaultTwitterConsumerSecret ?: TWITTER_CONSUMER_SECRET
            }
            defaultFeatures.save(preferences)
        }.fail {
            DebugLog.w(LOGTAG, "Unable to update default features", it)
        }.alwaysUi {
            val activity = weakThis.get() ?: return@alwaysUi
            if (activity.isFinishing) return@alwaysUi
            activity.executeAfterFragmentResumed { activity ->
                val fm = activity.supportFragmentManager
                val df = fm.findFragmentByTag(FRAGMENT_TAG_LOADING_DEFAULT_FEATURES) as? DialogFragment
                df?.dismiss()
            }
        }
    }

    private fun handleBrowserLoginResult(intent: Intent?) {
        if (intent == null) return
        if (signInTask?.status == AsyncTask.Status.RUNNING) {
            signInTask?.cancel(true)
        }
        val verifier = intent.getStringExtra(EXTRA_OAUTH_VERIFIER)
        val requestToken = OAuthToken(intent.getStringExtra(EXTRA_REQUEST_TOKEN),
                intent.getStringExtra(EXTRA_REQUEST_TOKEN_SECRET))
        signInTask = BrowserSignInTask(this, apiConfig, requestToken, verifier)
        AsyncTaskUtils.executeTask<AbstractSignInTask, Any>(signInTask)
    }

    private fun setDefaultAPI() {
        val apiLastChange = preferences.getLong(KEY_API_LAST_CHANGE, apiChangeTimestamp)
        val defaultApiChanged = apiLastChange != apiChangeTimestamp
        val apiUrlFormat = preferences.getNonEmptyString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getString(KEY_CREDENTIALS_TYPE, Credentials.Type.OAUTH)
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = preferences.getNonEmptyString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY)
        val consumerSecret = preferences.getNonEmptyString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET)
        if (TextUtils.isEmpty(apiConfig.apiUrlFormat) || defaultApiChanged) {
            apiConfig.apiUrlFormat = apiUrlFormat
        }
        if (defaultApiChanged) {
            apiConfig.credentialsType = authType
        }
        if (defaultApiChanged) {
            apiConfig.isSameOAuthUrl = sameOAuthSigningUrl
        }
        if (defaultApiChanged) {
            apiConfig.isNoVersionSuffix = noVersionSuffix
        }
        if (TextUtils.isEmpty(apiConfig.consumerKey) || defaultApiChanged) {
            apiConfig.consumerKey = consumerKey
        }
        if (TextUtils.isEmpty(apiConfig.consumerSecret) || defaultApiChanged) {
            apiConfig.consumerSecret = consumerSecret
        }
        if (defaultApiChanged) {
            apiChangeTimestamp = apiLastChange
        }
    }

    private fun setSignInButton() {
        when (apiConfig.credentialsType) {
            Credentials.Type.XAUTH, Credentials.Type.BASIC -> {
                passwordSignIn.visibility = View.GONE
                signIn.isEnabled = editPassword.text.isNotEmpty() && editUsername.text.isNotEmpty()
            }
            Credentials.Type.OAUTH -> {
                passwordSignIn.visibility = View.VISIBLE
                signIn.isEnabled = true
            }
            else -> {
                passwordSignIn.visibility = View.GONE
                signIn.isEnabled = true
            }
        }
        signUp.visibility = if (apiConfig.signUpUrlOrDefault != null) {
            View.VISIBLE
        } else {
            View.GONE
        }
        passwordSignIn.visibility = if (apiConfig.type == null || apiConfig.type == AccountType.TWITTER) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }


    private fun finishSignIn() {
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResult = Bundle {
                this[AccountManager.KEY_BOOLEAN_RESULT] = true
            }
        } else {
            val intent = Intent(this, HomeActivity::class.java)
            //TODO refresh timelines
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        finish()
    }

    internal abstract class AbstractSignInTask(activity: SignInActivity) : AsyncTask<Any, Runnable, SingleResponse<SignInResponse>>() {

        protected val activityRef = WeakReference(activity)
        protected val profileImageSize = activity.getString(R.string.profile_image_size)

        override final fun doInBackground(vararg args: Any?): SingleResponse<SignInResponse> {
            try {
                return SingleResponse.getInstance(performLogin())
            } catch (e: Exception) {
                return SingleResponse.getInstance(e)
            }
        }

        abstract fun performLogin(): SignInResponse

        override fun onPostExecute(result: SingleResponse<SignInResponse>) {
            val activity = activityRef.get()
            activity?.dismissDialogFragment(FRAGMENT_TAG_SIGN_IN_PROGRESS)
            if (result.hasData()) {
                activity?.onSignInResult(result.data!!)
            } else {
                activity?.onSignInError(result.exception!!)
            }
        }

        override fun onPreExecute() {
            val activity = activityRef.get()
            activity?.onSignInStart()
        }

        override fun onProgressUpdate(vararg values: Runnable) {
            for (value in values) {
                value.run()
            }
        }

        @Throws(MicroBlogException::class)
        internal fun analyseUserProfileColor(user: User?): Int {
            if (user == null) throw MicroBlogException("Unable to get user info")
            return ParcelableUserUtils.parseColor(user.profileLinkColor)
        }

    }

    /**
     * Created by mariotaku on 16/7/7.
     */
    internal class BrowserSignInTask(
            context: SignInActivity,
            private val apiConfig: CustomAPIConfig,
            private val requestToken: OAuthToken,
            private val oauthVerifier: String?
    ) : AbstractSignInTask(context) {

        private val context: Context

        init {
            this.context = context
        }

        @Throws(Exception::class)
        override fun performLogin(): SignInResponse {
            val versionSuffix = if (apiConfig.isNoVersionSuffix) null else "1.1"
            var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiConfig.apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            val oauth = newMicroBlogInstance(context, endpoint = endpoint,
                    auth = OAuthAuthorization(apiConfig.consumerKey, apiConfig.consumerSecret),
                    accountType = apiConfig.type,
                    cls = TwitterOAuth::class.java)
            val accessToken: OAuthToken
            if (oauthVerifier != null) {
                accessToken = oauth.getAccessToken(requestToken, oauthVerifier)
            } else {
                accessToken = oauth.getAccessToken(requestToken)
            }
            val auth = OAuthAuthorization(apiConfig.consumerKey,
                    apiConfig.consumerSecret, accessToken)
            endpoint = MicroBlogAPIFactory.getOAuthEndpoint(apiConfig.apiUrlFormat, "api", versionSuffix,
                    apiConfig.isSameOAuthUrl)

            val twitter = newMicroBlogInstance(context, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser = twitter.verifyCredentials()
            var color = analyseUserProfileColor(apiUser)
            val accountType = SignInActivity.detectAccountType(twitter, apiUser, apiConfig.type)
            val userId = apiUser.id!!
            val accountKey = UserKey(userId, UserKeyUtils.getUserHost(apiUser))
            val user = ParcelableUserUtils.fromUser(apiUser, accountKey,
                    profileImageSize = profileImageSize)
            val am = AccountManager.get(context)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = OAuthCredentials()
            credentials.api_url_format = apiConfig.apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            credentials.same_oauth_signing_url = apiConfig.isSameOAuthUrl

            credentials.consumer_key = apiConfig.consumerKey
            credentials.consumer_secret = apiConfig.consumerSecret
            credentials.access_token = accessToken.oauthToken
            credentials.access_token_secret = accessToken.oauthTokenSecret

            return SignInResponse(account != null, Credentials.Type.OAUTH, credentials, user, color,
                    accountType)
        }
    }

    /**
     * Created by mariotaku on 16/7/7.
     */
    class InputLoginVerificationDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener, DialogInterface.OnShowListener {

        private var callback: SignInTask.InputLoginVerificationCallback? = null
        var challengeType: String? = null

        internal fun setCallback(callback: SignInTask.InputLoginVerificationCallback) {
            this.callback = callback
        }


        override fun onCancel(dialog: DialogInterface?) {
            callback!!.challengeResponse = null
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.login_verification)
            builder.setView(R.layout.dialog_login_verification_code)
            builder.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, this)
            val dialog = builder.create()
            dialog.setOnShowListener(this)
            return dialog
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val alertDialog = dialog as AlertDialog
                    val editVerification = (alertDialog.findViewById(R.id.edit_verification_code) as EditText?)!!
                    callback!!.challengeResponse = ParseUtils.parseString(editVerification.text)
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                    callback!!.challengeResponse = null
                }
            }
        }

        override fun onShow(dialog: DialogInterface) {
            (dialog as AlertDialog).applyTheme()
            val verificationHint = dialog.findViewById(R.id.verification_hint) as TextView?
            val editVerification = dialog.findViewById(R.id.edit_verification_code) as EditText?
            if (verificationHint == null || editVerification == null) return
            when {
                "Push".equals(challengeType, ignoreCase = true) -> {
                    verificationHint.setText(R.string.login_verification_push_hint)
                    editVerification.visibility = View.GONE
                }
                "RetypePhoneNumber".equals(challengeType, ignoreCase = true) -> {
                    verificationHint.setText(R.string.login_challenge_retype_phone_hint)
                    editVerification.inputType = InputType.TYPE_CLASS_PHONE
                    editVerification.visibility = View.VISIBLE
                }
                "RetypeEmail".equals(challengeType, ignoreCase = true) -> {
                    verificationHint.setText(R.string.login_challenge_retype_email_hint)
                    editVerification.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    editVerification.visibility = View.VISIBLE
                }
                "Sms".equals(challengeType, ignoreCase = true) -> {
                    verificationHint.setText(R.string.login_verification_pin_hint)
                    editVerification.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    editVerification.visibility = View.VISIBLE
                }
                else -> {
                    verificationHint.text = getString(R.string.unsupported_login_verification_type_name,
                            challengeType)
                    editVerification.visibility = View.VISIBLE
                }
            }
        }
    }

    class PasswordSignInDialogFragment : BaseDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setView(R.layout.dialog_password_sign_in)
            builder.setPositiveButton(R.string.sign_in) { dialog, which ->
                val alertDialog = dialog as AlertDialog
                val editUsername = alertDialog.findViewById(R.id.username) as EditText?
                val editPassword = alertDialog.findViewById(R.id.password) as EditText?
                assert(editUsername != null && editPassword != null)
                val activity = activity as SignInActivity
                activity.setUsernamePassword(editUsername!!.text.toString(),
                        editPassword!!.text.toString())
                activity.doLogin()
            }
            builder.setNegativeButton(android.R.string.cancel, null)

            val alertDialog = builder.create()
            alertDialog.setOnShowListener {
                (it as AlertDialog)
                it.applyTheme()
                val editUsername = it.findViewById(R.id.username) as EditText?
                val editPassword = it.findViewById(R.id.password) as EditText?
                assert(editUsername != null && editPassword != null)
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val button = it.getButton(DialogInterface.BUTTON_POSITIVE) ?: return
                        button.isEnabled = editUsername!!.length() > 0 && editPassword!!.length() > 0
                    }

                    override fun afterTextChanged(s: Editable) {

                    }
                }

                editUsername!!.addTextChangedListener(textWatcher)
                editPassword!!.addTextChangedListener(textWatcher)
            }
            return alertDialog
        }
    }

    internal data class SignInResponse(
            val alreadyLoggedIn: Boolean,
            @Credentials.Type val credsType: String = Credentials.Type.EMPTY,
            val credentials: Credentials,
            val user: ParcelableUser,
            val color: Int = 0,
            val accountType: Pair<String, AccountExtras?>
    ) {

        private fun writeAccountInfo(action: (k: String, v: String?) -> Unit) {
            action(ACCOUNT_USER_DATA_KEY, user.key.toString())
            action(ACCOUNT_USER_DATA_TYPE, accountType.first)
            action(ACCOUNT_USER_DATA_CREDS_TYPE, credsType)

            action(ACCOUNT_USER_DATA_ACTIVATED, true.toString())
            action(ACCOUNT_USER_DATA_COLOR, toHexColor(color, format = HexColorFormat.RGB))

            action(ACCOUNT_USER_DATA_USER, LoganSquare.serialize(user))
            action(ACCOUNT_USER_DATA_EXTRAS, accountType.second?.let { LoganSquare.serialize(it) })
        }

        private fun writeAuthToken(am: AccountManager, account: Account) {
            val authToken = LoganSquare.serialize(credentials)
            am.setAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE, authToken)
        }

        fun updateAccount(am: AccountManager) {
            val account = AccountUtils.findByAccountKey(am, user.key) ?: return
            writeAccountInfo { k, v ->
                am.setUserData(account, k, v)
            }
            writeAuthToken(am, account)
        }

        fun addAccount(am: AccountManager, randomizeAccountName: Boolean): Account {
            var accountName: String
            if (randomizeAccountName) {
                val usedNames = ArraySet<String>()
                AccountUtils.getAccounts(am).mapTo(usedNames, Account::name)
                do {
                    accountName = UUID.randomUUID().toString()
                } while (accountName in usedNames)
            } else {
                accountName = generateAccountName(user.screen_name, user.key.host)
            }
            val account = Account(accountName, ACCOUNT_TYPE)
            val accountPosition = AccountUtils.getAccounts(am).size
            // Don't add UserData in this method, see http://stackoverflow.com/a/29776224/859190
            am.addAccountExplicitly(account, null, null)
            writeAccountInfo { k, v ->
                am.setUserData(account, k, v)
            }
            am.setUserData(account, ACCOUNT_USER_DATA_POSITION, accountPosition.toString())
            writeAuthToken(am, account)
            return account
        }

    }

    internal class SignInTask(
            activity: SignInActivity,
            private val username: String,
            private val password: String,
            private val apiConfig: CustomAPIConfig
    ) : AbstractSignInTask(activity) {
        private val verificationCallback = InputLoginVerificationCallback()
        private val userAgent = UserAgentUtils.getDefaultUserAgentString(activity)
        private val apiUrlFormat = apiConfig.apiUrlFormat ?: DEFAULT_TWITTER_API_URL_FORMAT

        @Throws(Exception::class)
        override fun performLogin(): SignInResponse {
            when (apiConfig.credentialsType) {
                Credentials.Type.OAUTH -> return authOAuth()
                Credentials.Type.XAUTH -> return authxAuth()
                Credentials.Type.BASIC -> return authBasic()
                Credentials.Type.EMPTY -> return authTwipOMode()
            }
            return authOAuth()
        }

        @Throws(OAuthPasswordAuthenticator.AuthenticationException::class, MicroBlogException::class)
        private fun authOAuth(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            val endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            val auth = OAuthAuthorization(apiConfig.consumerKey,
                    apiConfig.consumerSecret)
            val oauth = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = TwitterOAuth::class.java)
            val authenticator = OAuthPasswordAuthenticator(oauth,
                    verificationCallback, userAgent)
            val accessToken = authenticator.getOAuthAccessToken(username, password)
            val userId = accessToken.userId!!
            return getOAuthSignInResponse(activity, accessToken, userId,
                    Credentials.Type.OAUTH)
        }

        @Throws(MicroBlogException::class)
        private fun authxAuth(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            var auth = OAuthAuthorization(apiConfig.consumerKey,
                    apiConfig.consumerSecret)
            val oauth = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = TwitterOAuth::class.java)
            val accessToken = oauth.getAccessToken(username, password)
            var userId: String? = accessToken.userId
            if (userId == null) {
                // Trying to fix up userId if accessToken doesn't contain one.
                auth = OAuthAuthorization(apiConfig.consumerKey,
                        apiConfig.consumerSecret, accessToken)
                endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat, apiConfig.isSameOAuthUrl,
                        apiConfig.isNoVersionSuffix)
                val microBlog = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                        accountType = apiConfig.type, cls = MicroBlog::class.java)
                userId = microBlog.verifyCredentials().id
            }
            return getOAuthSignInResponse(activity, accessToken, userId!!, Credentials.Type.XAUTH)
        }

        @Throws(MicroBlogException::class, OAuthPasswordAuthenticator.AuthenticationException::class)
        private fun authBasic(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            val versionSuffix = if (apiConfig.isNoVersionSuffix) null else "1.1"
            val endpoint = Endpoint(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, "api",
                    versionSuffix))
            val auth = BasicAuthorization(username, password)
            val twitter = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser: User
            try {
                apiUser = twitter.verifyCredentials()
            } catch (e: MicroBlogException) {
                if (e.statusCode == 401) {
                    throw WrongBasicCredentialException()
                } else if (e.statusCode == 404) {
                    throw WrongAPIURLFormatException()
                }
                throw e
            }

            val userId = apiUser.id!!
            var color = analyseUserProfileColor(apiUser)
            val accountType = SignInActivity.detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = UserKey(userId, UserKeyUtils.getUserHost(apiUser))
            val user = ParcelableUserUtils.fromUser(apiUser, accountKey,
                    profileImageSize = profileImageSize)
            val am = AccountManager.get(activity)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = BasicCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix
            credentials.username = username
            credentials.password = password
            return SignInResponse(account != null, Credentials.Type.BASIC, credentials, user,
                    color, accountType)
        }


        @Throws(MicroBlogException::class)
        private fun authTwipOMode(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            val versionSuffix = if (apiConfig.isNoVersionSuffix) null else "1.1"
            val endpoint = Endpoint(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, "api",
                    versionSuffix))
            val auth = EmptyAuthorization()
            val twitter = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser = twitter.verifyCredentials()
            val userId = apiUser.id!!
            var color = analyseUserProfileColor(apiUser)
            val accountType = SignInActivity.detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = UserKey(userId, UserKeyUtils.getUserHost(apiUser))
            val user = ParcelableUserUtils.fromUser(apiUser, accountKey,
                    profileImageSize = profileImageSize)
            val am = AccountManager.get(activity)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = EmptyCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            return SignInResponse(account != null, Credentials.Type.EMPTY, credentials, user, color,
                    accountType)
        }

        @Throws(MicroBlogException::class)
        private fun getOAuthSignInResponse(activity: SignInActivity,
                accessToken: OAuthToken,
                userId: String, @Credentials.Type authType: String): SignInResponse {
            val auth = OAuthAuthorization(apiConfig.consumerKey,
                    apiConfig.consumerSecret, accessToken)
            val endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl, apiConfig.isNoVersionSuffix)
            val twitter = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser = twitter.verifyCredentials()
            var color = analyseUserProfileColor(apiUser)
            val accountType = SignInActivity.detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = UserKey(userId, UserKeyUtils.getUserHost(apiUser))
            val user = ParcelableUserUtils.fromUser(apiUser, accountKey,
                    profileImageSize = profileImageSize)
            val am = AccountManager.get(activity)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = OAuthCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            credentials.same_oauth_signing_url = apiConfig.isSameOAuthUrl

            credentials.consumer_key = apiConfig.consumerKey
            credentials.consumer_secret = apiConfig.consumerSecret
            credentials.access_token = accessToken.oauthToken
            credentials.access_token_secret = accessToken.oauthTokenSecret

            return SignInResponse(account != null, authType, credentials, user, color, accountType)
        }

        internal class WrongBasicCredentialException : OAuthPasswordAuthenticator.AuthenticationException()

        internal class WrongAPIURLFormatException : OAuthPasswordAuthenticator.AuthenticationException()

        internal inner class InputLoginVerificationCallback : OAuthPasswordAuthenticator.LoginVerificationCallback {

            var isChallengeFinished: Boolean = false

            var challengeResponse: String? = null
                set(value) {
                    isChallengeFinished = true
                    field = value
                }

            override fun getLoginVerification(challengeType: String): String? {
                // Dismiss current progress dialog
                publishProgress(Runnable {
                    activityRef.get()?.dismissDialogFragment(SignInActivity.FRAGMENT_TAG_SIGN_IN_PROGRESS)
                })
                // Show verification input dialog and wait for user input
                publishProgress(Runnable {
                    val activity = activityRef.get() ?: return@Runnable
                    activity.executeAfterFragmentResumed { activity ->
                        val sia = activity as SignInActivity
                        val df = InputLoginVerificationDialogFragment()
                        df.isCancelable = false
                        df.setCallback(this@InputLoginVerificationCallback)
                        df.challengeType = challengeType
                        df.show(sia.supportFragmentManager, "login_challenge_$challengeType")
                    }
                })
                while (!isChallengeFinished) {
                    // Wait for 50ms
                    try {
                        Thread.sleep(50)
                    } catch (e: InterruptedException) {
                        // Ignore
                    }

                }
                // Show progress dialog
                publishProgress(Runnable {
                    val activity = activityRef.get() ?: return@Runnable
                    activity.showSignInProgressDialog()
                })
                return challengeResponse
            }

        }

    }


    companion object {

        private val FRAGMENT_TAG_SIGN_IN_PROGRESS = "sign_in_progress"
        private val FRAGMENT_TAG_LOADING_DEFAULT_FEATURES = "loading_default_features"
        private val EXTRA_API_LAST_CHANGE = "api_last_change"
        private val DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/"

        @Throws(IOException::class)
        internal fun detectAccountType(twitter: MicroBlog, user: User, type: String?): Pair<String, AccountExtras?> {
            when (type) {
                AccountType.STATUSNET -> {
                    return getStatusNetAccountType(twitter)
                }
                AccountType.TWITTER -> {
                    return getTwitterAccountType(twitter)
                }
                AccountType.FANFOU -> {
                    return Pair(AccountType.FANFOU, null)
                }
                else -> {
                    if (UserKeyUtils.isFanfouUser(user)) {
                        return Pair(AccountType.FANFOU, null)
                    }
                }
            }
            return Pair(AccountType.TWITTER, null)
        }

        private fun getStatusNetAccountType(twitter: MicroBlog): Pair<String, StatusNetAccountExtras> {
            // Get StatusNet specific resource
            val config = twitter.statusNetConfig
            val extras = StatusNetAccountExtras()
            val site = config.site
            if (site != null) {
                extras.textLimit = site.textLimit
            }
            return Pair(AccountType.STATUSNET, extras)
        }

        private fun getTwitterAccountType(twitter: MicroBlog): Pair<String, TwitterAccountExtras> {
            val extras = TwitterAccountExtras()
            try {
                // Get Twitter official only resource
                val paging = Paging()
                paging.count(1)
                twitter.getActivitiesAboutMe(paging)
                extras.setIsOfficialCredentials(true)
            } catch (e: MicroBlogException) {
                // Ignore
            }
            return Pair(AccountType.TWITTER, extras)
        }
    }

    private val CustomAPIConfig.signUpUrlOrDefault: String?
        get() = signUpUrl ?: when (type) {
            AccountType.TWITTER -> "https://twitter.com/signup"
            AccountType.FANFOU -> "http://fanfou.com/register"
            else -> null
        }

}
