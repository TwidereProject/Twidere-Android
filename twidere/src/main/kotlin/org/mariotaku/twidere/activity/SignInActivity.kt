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

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.rengwuxian.materialedittext.MaterialEditText
import kotlinx.android.synthetic.main.activity_sign_in.*
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
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.iface.APIEditorActivity
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.ParcelableCredentials.AuthType
import org.mariotaku.twidere.model.util.ParcelableAccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.model.util.UserKeyUtils
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.*
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator
import java.lang.ref.WeakReference

class SignInActivity : BaseActivity(), OnClickListener, TextWatcher {
    private var apiUrlFormat: String? = null
    private var authType: Int = 0
    private var consumerKey: String? = null
    private var consumerSecret: String? = null
    private var apiChangeTimestamp: Long = 0
    private var sameOAuthSigningUrl: Boolean = false
    private var noVersionSuffix: Boolean = false
    private var signInTask: AbstractSignInTask? = null

    override fun afterTextChanged(s: Editable) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_API -> {
                if (resultCode == Activity.RESULT_OK) {
                    apiUrlFormat = data!!.getStringExtra(Accounts.API_URL_FORMAT)
                    authType = data.getIntExtra(Accounts.AUTH_TYPE, AuthType.OAUTH)
                    sameOAuthSigningUrl = data.getBooleanExtra(Accounts.SAME_OAUTH_SIGNING_URL, false)
                    noVersionSuffix = data.getBooleanExtra(Accounts.NO_VERSION_SUFFIX, false)
                    consumerKey = data.getStringExtra(Accounts.CONSUMER_KEY)
                    consumerSecret = data.getStringExtra(Accounts.CONSUMER_SECRET)
                    updateSignInType()
                }
                setSignInButton()
                invalidateOptionsMenu()
            }
            REQUEST_BROWSER_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    doBrowserLogin(data)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    internal fun updateSignInType() {
        when (authType) {
            AuthType.XAUTH, AuthType.BASIC -> {
                usernamePasswordContainer.visibility = View.VISIBLE
                signInSignUpContainer.orientation = LinearLayout.HORIZONTAL
            }
            AuthType.TWIP_O_MODE -> {
                usernamePasswordContainer.visibility = View.GONE
                signInSignUpContainer.orientation = LinearLayout.VERTICAL
            }
            else -> {
                usernamePasswordContainer.visibility = View.GONE
                signInSignUpContainer.orientation = LinearLayout.VERTICAL
            }
        }
    }

    override fun onClick(v: View) {
        when (v) {
            signUp -> {
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(TWITTER_SIGNUP_URL))
                startActivity(intent)
            }
            signIn -> {
                if (usernamePasswordContainer.visibility != View.VISIBLE) {
                    editUsername.text = null
                    editPassword.text = null
                }
                doLogin()
            }
            passwordSignIn -> {
                executeAfterFragmentResumed {
                    val fm = supportFragmentManager
                    val df = PasswordSignInDialogFragment()
                    df.show(fm.beginTransaction(), "password_sign_in")
                    Unit
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sign_in, menu)
        return true
    }

    public override fun onDestroy() {
        loaderManager.destroyLoader(0)
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val accountKeys = DataStoreUtils.getActivatedAccountKeys(this)
                if (accountKeys.size > 0) {
                    onBackPressed()
                }
            }
            R.id.settings -> {
                if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
                    return false
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
            R.id.edit_api -> {
                if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
                    return false
                setDefaultAPI()
                val intent = Intent(this, APIEditorActivity::class.java)
                intent.putExtra(Accounts.API_URL_FORMAT, apiUrlFormat)
                intent.putExtra(Accounts.AUTH_TYPE, authType)
                intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
                intent.putExtra(Accounts.NO_VERSION_SUFFIX, noVersionSuffix)
                intent.putExtra(Accounts.CONSUMER_KEY, consumerKey)
                intent.putExtra(Accounts.CONSUMER_SECRET, consumerSecret)
                startActivityForResult(intent, REQUEST_EDIT_API)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    internal fun openBrowserLogin(): Boolean {
        if (authType != AuthType.OAUTH || signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING)
            return true
        val intent = Intent(this, BrowserSignInActivity::class.java)
        intent.putExtra(Accounts.CONSUMER_KEY, consumerKey)
        intent.putExtra(Accounts.CONSUMER_SECRET, consumerSecret)
        intent.putExtra(Accounts.API_URL_FORMAT, apiUrlFormat)
        intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
        startActivityForResult(intent, REQUEST_BROWSER_SIGN_IN)
        return false
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val itemBrowser = menu.findItem(R.id.open_in_browser)
        if (itemBrowser != null) {
            val is_oauth = authType == AuthType.OAUTH
            itemBrowser.isVisible = is_oauth
            itemBrowser.isEnabled = is_oauth
        }
        return true
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        setDefaultAPI()
        outState.putString(Accounts.API_URL_FORMAT, apiUrlFormat)
        outState.putInt(Accounts.AUTH_TYPE, authType)
        outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
        outState.putBoolean(Accounts.NO_VERSION_SUFFIX, noVersionSuffix)
        outState.putString(Accounts.CONSUMER_KEY, consumerKey)
        outState.putString(Accounts.CONSUMER_SECRET, consumerSecret)
        outState.putLong(EXTRA_API_LAST_CHANGE, apiChangeTimestamp)
        super.onSaveInstanceState(outState)
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        setSignInButton()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        if (savedInstanceState != null) {
            apiUrlFormat = savedInstanceState.getString(Accounts.API_URL_FORMAT)
            authType = savedInstanceState.getInt(Accounts.AUTH_TYPE)
            sameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL)
            consumerKey = Utils.trim(savedInstanceState.getString(Accounts.CONSUMER_KEY))
            consumerSecret = Utils.trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET))
            apiChangeTimestamp = savedInstanceState.getLong(EXTRA_API_LAST_CHANGE)
        }

        val isTwipOMode = authType == AuthType.TWIP_O_MODE
        usernamePasswordContainer.visibility = if (isTwipOMode) View.GONE else View.VISIBLE
        signInSignUpContainer.orientation = if (isTwipOMode) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL

        editUsername.addTextChangedListener(this)
        editPassword.addTextChangedListener(this)

        signIn.setOnClickListener(this)
        signUp.setOnClickListener(this)
        passwordSignIn.setOnClickListener(this)

        val color = ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.material_light_green))
        ViewCompat.setBackgroundTintList(signIn, color)


        val consumerKey = preferences.getString(KEY_CONSUMER_KEY, null)
        val consumerSecret = preferences.getString(KEY_CONSUMER_SECRET, null)
        if (BuildConfig.SHOW_CUSTOM_TOKEN_DIALOG && savedInstanceState == null &&
                !preferences.getBoolean(KEY_CONSUMER_KEY_SECRET_SET, false) &&
                !Utils.isCustomConsumerKeySecret(consumerKey, consumerSecret)) {
            val df = SetConsumerKeySecretDialogFragment()
            df.isCancelable = false
            df.show(supportFragmentManager, "set_consumer_key_secret")
        }

        updateSignInType()
        setSignInButton()
    }

    internal fun doLogin() {
        if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING) {
            signInTask!!.cancel(true)
        }
        setDefaultAPI()
        if (authType == AuthType.OAUTH && editUsername.length() <= 0) {
            openBrowserLogin()
            return
        }
        val consumerKey = MicroBlogAPIFactory.getOAuthToken(this.consumerKey, consumerSecret)
        val apiUrlFormat = if (TextUtils.isEmpty(this.apiUrlFormat)) DEFAULT_TWITTER_API_URL_FORMAT else this.apiUrlFormat!!
        val username = editUsername.text.toString()
        val password = editPassword.text.toString()
        signInTask = SignInTask(this, username, password, authType, consumerKey, apiUrlFormat,
                sameOAuthSigningUrl, noVersionSuffix)
        AsyncTaskUtils.executeTask<AbstractSignInTask, Any>(signInTask)
    }

    private fun doBrowserLogin(intent: Intent?) {
        if (intent == null) return
        if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING) {
            signInTask!!.cancel(true)
        }
        setDefaultAPI()
        val verifier = intent.getStringExtra(EXTRA_OAUTH_VERIFIER)
        val consumerKey = MicroBlogAPIFactory.getOAuthToken(this.consumerKey, consumerSecret)
        val requestToken = OAuthToken(intent.getStringExtra(EXTRA_REQUEST_TOKEN),
                intent.getStringExtra(EXTRA_REQUEST_TOKEN_SECRET))
        val apiUrlFormat = if (TextUtils.isEmpty(this.apiUrlFormat)) DEFAULT_TWITTER_API_URL_FORMAT else this.apiUrlFormat!!
        signInTask = BrowserSignInTask(this, consumerKey, requestToken, verifier, apiUrlFormat,
                sameOAuthSigningUrl, noVersionSuffix)
        AsyncTaskUtils.executeTask<AbstractSignInTask, Any>(signInTask)
    }


    private fun setDefaultAPI() {
        val apiLastChange = preferences.getLong(KEY_API_LAST_CHANGE, apiChangeTimestamp)
        val defaultApiChanged = apiLastChange != apiChangeTimestamp
        val apiUrlFormat = Utils.getNonEmptyString(preferences, KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getInt(KEY_AUTH_TYPE, AuthType.OAUTH)
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = Utils.getNonEmptyString(preferences, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY)
        val consumerSecret = Utils.getNonEmptyString(preferences, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET)
        if (TextUtils.isEmpty(this.apiUrlFormat) || defaultApiChanged) {
            this.apiUrlFormat = apiUrlFormat
        }
        if (defaultApiChanged) {
            this.authType = authType
        }
        if (defaultApiChanged) {
            this.sameOAuthSigningUrl = sameOAuthSigningUrl
        }
        if (defaultApiChanged) {
            this.noVersionSuffix = noVersionSuffix
        }
        if (TextUtils.isEmpty(this.consumerKey) || defaultApiChanged) {
            this.consumerKey = consumerKey
        }
        if (TextUtils.isEmpty(this.consumerSecret) || defaultApiChanged) {
            this.consumerSecret = consumerSecret
        }
        if (defaultApiChanged) {
            apiChangeTimestamp = apiLastChange
        }
    }

    private fun setSignInButton() {
        when (authType) {
            AuthType.XAUTH, AuthType.BASIC -> {
                passwordSignIn.visibility = View.GONE
                signIn.isEnabled = editPassword.text.length > 0 && editUsername.text.length > 0
            }
            AuthType.OAUTH -> {
                passwordSignIn.visibility = View.VISIBLE
                signIn.isEnabled = true
            }
            else -> {
                passwordSignIn.visibility = View.GONE
                signIn.isEnabled = true
            }
        }
    }

    internal fun onSignInResult(result: SignInResponse?) {
        dismissDialogFragment(FRAGMENT_TAG_SIGN_IN_PROGRESS)
        if (result != null) {
            if (result.alreadyLoggedIn) {
                val values = result.toContentValues()
                if (values != null) {
                    val where = Expression.equalsArgs(Accounts.ACCOUNT_KEY).sql
                    val whereArgs = arrayOf(values.getAsString(Accounts.ACCOUNT_KEY))
                    contentResolver.update(Accounts.CONTENT_URI, values, where, whereArgs)
                }
                Toast.makeText(this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show()
            } else if (result.succeed) {
                val values = result.toContentValues()
                if (values != null) {
                    contentResolver.insert(Accounts.CONTENT_URI, values)
                }
                val intent = Intent(this, HomeActivity::class.java)
                //TODO refresh time lines
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                finish()
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, result.exception)
                }
                if (result.exception is AuthenticityTokenException) {
                    Toast.makeText(this, R.string.wrong_api_key, Toast.LENGTH_SHORT).show()
                } else if (result.exception is WrongUserPassException) {
                    Toast.makeText(this, R.string.wrong_username_password, Toast.LENGTH_SHORT).show()
                } else if (result.exception is SignInTask.WrongBasicCredentialException) {
                    Toast.makeText(this, R.string.wrong_username_password, Toast.LENGTH_SHORT).show()
                } else if (result.exception is SignInTask.WrongAPIURLFormatException) {
                    Toast.makeText(this, R.string.wrong_api_key, Toast.LENGTH_SHORT).show()
                } else if (result.exception is LoginVerificationException) {
                    Toast.makeText(this, R.string.login_verification_failed, Toast.LENGTH_SHORT).show()
                } else if (result.exception is AuthenticationException) {
                    Utils.showErrorMessage(this, getString(R.string.action_signing_in), result.exception.cause, true)
                } else {
                    Utils.showErrorMessage(this, getString(R.string.action_signing_in), result.exception, true)
                }
            }
        }
        setSignInButton()
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

    internal abstract class AbstractSignInTask(activity: SignInActivity) : AsyncTask<Any, Runnable, SignInResponse>() {

        protected val activityRef: WeakReference<SignInActivity>

        init {
            this.activityRef = WeakReference(activity)
        }

        override fun onPostExecute(result: SignInResponse) {
            val activity = activityRef.get()
            activity?.onSignInResult(result)
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
            return ParseUtils.parseColor("#" + user.profileLinkColor, Color.TRANSPARENT)
        }

    }

    /**
     * Created by mariotaku on 16/7/7.
     */
    internal class BrowserSignInTask(
            context: SignInActivity,
            private val consumerKey: OAuthToken,
            private val requestToken: OAuthToken,
            private val oauthVerifier: String?,
            private val apiUrlFormat: String,
            private val sameOauthSigningUrl: Boolean,
            private val noVersionSuffix: Boolean
    ) : AbstractSignInTask(context) {

        private val context: Context

        init {
            this.context = context
        }

        override fun doInBackground(vararg params: Any): SignInResponse {
            try {
                val versionSuffix = if (noVersionSuffix) null else "1.1"
                var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                        sameOauthSigningUrl)
                val oauth = MicroBlogAPIFactory.getInstance(context, endpoint,
                        OAuthAuthorization(consumerKey.oauthToken,
                                consumerKey.oauthTokenSecret), TwitterOAuth::class.java)
                val accessToken: OAuthToken
                if (oauthVerifier != null) {
                    accessToken = oauth.getAccessToken(requestToken, oauthVerifier)
                } else {
                    accessToken = oauth.getAccessToken(requestToken)
                }
                val auth = OAuthAuthorization(consumerKey.oauthToken,
                        consumerKey.oauthTokenSecret, accessToken)
                endpoint = MicroBlogAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", versionSuffix,
                        sameOauthSigningUrl)
                val twitter = MicroBlogAPIFactory.getInstance(context, endpoint, auth, MicroBlog::class.java)
                val user = twitter.verifyCredentials()
                var color = analyseUserProfileColor(user)
                val accountType = SignInActivity.detectAccountType(twitter, user)
                val account = ParcelableAccountUtils.getAccount(context,
                        UserKey(user.id, UserKeyUtils.getUserHost(user)))
                if (account != null) {
                    color = account.color
                }
                return SignInResponse(account != null, auth, user, ParcelableCredentials.AuthType.OAUTH, color,
                        apiUrlFormat, sameOauthSigningUrl, noVersionSuffix, accountType)
            } catch (e: MicroBlogException) {
                return SignInResponse(false, false, e)
            }

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
            val alertDialog = dialog as AlertDialog
            val verificationHint = alertDialog.findViewById(R.id.verification_hint) as TextView?
            val editVerification = alertDialog.findViewById(R.id.edit_verification_code) as EditText?
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
            alertDialog.setOnShowListener { dialog ->
                val materialDialog = dialog as AlertDialog
                val editUsername = materialDialog.findViewById(R.id.username) as EditText?
                val editPassword = materialDialog.findViewById(R.id.password) as EditText?
                assert(editUsername != null && editPassword != null)
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val button = materialDialog.getButton(DialogInterface.BUTTON_POSITIVE) ?: return
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

    class SetConsumerKeySecretDialogFragment : BaseDialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setView(R.layout.dialog_set_consumer_key_secret)
            builder.setPositiveButton(android.R.string.ok) { dialog, which ->
                val editConsumerKey = (dialog as Dialog).findViewById(R.id.editConsumerKey) as EditText
                val editConsumerSecret = dialog.findViewById(R.id.editConsumerSecret) as EditText
                val prefs = SharedPreferencesWrapper.getInstance(activity, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putString(KEY_CONSUMER_KEY, ParseUtils.parseString(editConsumerKey.text))
                editor.putString(KEY_CONSUMER_SECRET, ParseUtils.parseString(editConsumerSecret.text))
                editor.apply()
            }
            val dialog = builder.create()
            dialog.setOnShowListener(DialogInterface.OnShowListener { dialog ->
                val activity = activity ?: return@OnShowListener
                val editConsumerKey = (dialog as Dialog).findViewById(R.id.editConsumerKey) as MaterialEditText
                val editConsumerSecret = dialog.findViewById(R.id.editConsumerSecret) as MaterialEditText
                editConsumerKey.addValidator(ConsumerKeySecretValidator(getString(R.string.invalid_consumer_key)))
                editConsumerSecret.addValidator(ConsumerKeySecretValidator(getString(R.string.invalid_consumer_secret)))
                val prefs = SharedPreferencesWrapper.getInstance(activity, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                editConsumerKey.setText(prefs.getString(KEY_CONSUMER_KEY, null))
                editConsumerSecret.setText(prefs.getString(KEY_CONSUMER_SECRET, null))
            })
            return dialog
        }
    }

    internal data class SignInResponse(
            val alreadyLoggedIn: Boolean,
            val succeed: Boolean,
            val exception: Exception?,
            val basicUsername: String? = null,
            val basicPassword: String? = null,
            val oauth: OAuthAuthorization? = null,
            val user: User? = null,
            val authType: Int = 0,
            val color: Int = 0,
            val apiUrlFormat: String? = null,
            val sameOAuthSigningUrl: Boolean = false,
            val noVersionSuffix: Boolean = false,
            val accountType: Pair<String, String>? = null
    ) {

        constructor(alreadyLoggedIn: Boolean, oauth: OAuthAuthorization,
                    user: User, authType: Int, color: Int,
                    apiUrlFormat: String, sameOAuthSigningUrl: Boolean,
                    noVersionSuffix: Boolean, accountType: Pair<String, String>) : this(alreadyLoggedIn, true, null, null, null, oauth, user, authType, color, apiUrlFormat,
                sameOAuthSigningUrl, noVersionSuffix, accountType) {
        }

        constructor(alreadyLoggedIn: Boolean, basicUsername: String,
                    basicPassword: String, user: User, color: Int,
                    apiUrlFormat: String, noVersionSuffix: Boolean,
                    accountType: Pair<String, String>) : this(alreadyLoggedIn, true, null, basicUsername, basicPassword, null, user,
                ParcelableCredentials.AuthType.BASIC, color, apiUrlFormat, false,
                noVersionSuffix, accountType) {
        }

        constructor(alreadyLoggedIn: Boolean, user: User, color: Int,
                    apiUrlFormat: String, noVersionSuffix: Boolean,
                    accountType: Pair<String, String>) : this(alreadyLoggedIn, true, null, null, null, null, user,
                ParcelableCredentials.AuthType.TWIP_O_MODE, color, apiUrlFormat, false,
                noVersionSuffix, accountType) {
        }

        fun toContentValues(): ContentValues? {
            if (user == null) return null
            val values: ContentValues
            when (authType) {
                ParcelableCredentials.AuthType.BASIC -> {
                    values = ContentValues()
                    values.put(Accounts.BASIC_AUTH_USERNAME, basicUsername)
                    values.put(Accounts.BASIC_AUTH_PASSWORD, basicPassword)
                    values.put(Accounts.AUTH_TYPE, ParcelableCredentials.AuthType.BASIC)
                }
                ParcelableCredentials.AuthType.TWIP_O_MODE -> {
                    values = ContentValues()
                    values.put(Accounts.AUTH_TYPE, ParcelableCredentials.AuthType.TWIP_O_MODE)
                }
                ParcelableCredentials.AuthType.OAUTH, ParcelableCredentials.AuthType.XAUTH -> {
                    values = ContentValues()
                    val accessToken = oauth!!.oauthToken
                    values.put(Accounts.OAUTH_TOKEN, accessToken.oauthToken)
                    values.put(Accounts.OAUTH_TOKEN_SECRET, accessToken.oauthTokenSecret)
                    values.put(Accounts.CONSUMER_KEY, oauth.consumerKey)
                    values.put(Accounts.CONSUMER_SECRET, oauth.consumerSecret)
                    values.put(Accounts.AUTH_TYPE, authType)
                }
                else -> {
                    return null
                }
            }

            values.put(Accounts.ACCOUNT_KEY, UserKeyUtils.fromUser(user).toString())
            values.put(Accounts.SCREEN_NAME, user.screenName)
            values.put(Accounts.NAME, user.name)
            values.put(Accounts.PROFILE_IMAGE_URL, TwitterContentUtils.getProfileImageUrl(user))
            values.put(Accounts.PROFILE_BANNER_URL, user.profileBannerImageUrl)

            values.put(Accounts.COLOR, color)
            values.put(Accounts.IS_ACTIVATED, 1)


            values.put(Accounts.API_URL_FORMAT, apiUrlFormat)
            values.put(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl)
            values.put(Accounts.NO_VERSION_SUFFIX, noVersionSuffix)

            if (accountType != null) {
                values.put(Accounts.ACCOUNT_TYPE, accountType.first)
                values.put(Accounts.ACCOUNT_EXTRAS, accountType.second)
                val accountKey = UserKeyUtils.fromUser(user)
                val parcelableUser = ParcelableUserUtils.fromUser(user, accountKey)
                values.put(Accounts.ACCOUNT_USER, JsonSerializer.serialize(parcelableUser, ParcelableUser::class.java))
            }
            return values
        }
    }

    internal class SignInTask(activity: SignInActivity, private val username: String,
                              private val password: String, private val authType: Int, private val consumerKey: OAuthToken,
                              private val apiUrlFormat: String, private val sameOAuthSigningUrl: Boolean,
                              private val noVersionSuffix: Boolean) : AbstractSignInTask(activity) {
        private val verificationCallback: InputLoginVerificationCallback
        private val userAgent: String

        init {
            verificationCallback = InputLoginVerificationCallback()
            userAgent = UserAgentUtils.getDefaultUserAgentString(activity)
        }

        override fun doInBackground(vararg params: Any): SignInResponse {
            try {
                when (authType) {
                    ParcelableCredentials.AuthType.OAUTH -> return authOAuth()
                    ParcelableCredentials.AuthType.XAUTH -> return authxAuth()
                    ParcelableCredentials.AuthType.BASIC -> return authBasic()
                    ParcelableCredentials.AuthType.TWIP_O_MODE -> return authTwipOMode()
                }
                return authOAuth()
            } catch (e: MicroBlogException) {
                Log.w(TwidereConstants.LOGTAG, e)
                return SignInResponse(false, false, e)
            } catch (e: OAuthPasswordAuthenticator.AuthenticationException) {
                Log.w(TwidereConstants.LOGTAG, e)
                return SignInResponse(false, false, e)
            }

        }

        @Throws(OAuthPasswordAuthenticator.AuthenticationException::class, MicroBlogException::class)
        private fun authOAuth(): SignInResponse {
            val activity = activityRef.get() ?: return SignInResponse(false, false, null)
            val endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    sameOAuthSigningUrl)
            val auth = OAuthAuthorization(consumerKey.oauthToken,
                    consumerKey.oauthTokenSecret)
            val oauth = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, TwitterOAuth::class.java)
            val authenticator = OAuthPasswordAuthenticator(oauth,
                    verificationCallback, userAgent)
            val accessToken = authenticator.getOAuthAccessToken(username, password)
            val userId = accessToken.userId ?: return SignInResponse(false, false, null)
            return getOAuthSignInResponse(activity, accessToken, userId,
                    ParcelableCredentials.AuthType.OAUTH)
        }

        @Throws(MicroBlogException::class)
        private fun authxAuth(): SignInResponse {
            val activity = activityRef.get() ?: return SignInResponse(false, false, null)
            var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    sameOAuthSigningUrl)
            var auth = OAuthAuthorization(consumerKey.oauthToken,
                    consumerKey.oauthTokenSecret)
            val oauth = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, TwitterOAuth::class.java)
            val accessToken = oauth.getAccessToken(username, password)
            var userId: String? = accessToken.userId
            if (userId == null) {
                // Trying to fix up userId if accessToken doesn't contain one.
                auth = OAuthAuthorization(consumerKey.oauthToken,
                        consumerKey.oauthTokenSecret, accessToken)
                endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat, sameOAuthSigningUrl,
                        noVersionSuffix)
                val microBlog = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, MicroBlog::class.java)
                userId = microBlog.verifyCredentials().id
            }
            if (userId == null) return SignInResponse(false, false, null)
            return getOAuthSignInResponse(activity, accessToken, userId,
                    ParcelableCredentials.AuthType.XAUTH)
        }

        @Throws(MicroBlogException::class, OAuthPasswordAuthenticator.AuthenticationException::class)
        private fun authBasic(): SignInResponse {
            val activity = activityRef.get() ?: return SignInResponse(false, false, null)
            val versionSuffix = if (noVersionSuffix) null else "1.1"
            val endpoint = Endpoint(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, "api",
                    versionSuffix))
            val auth = BasicAuthorization(username, password)
            val twitter = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, MicroBlog::class.java)
            val user: User
            try {
                user = twitter.verifyCredentials()
            } catch (e: MicroBlogException) {
                if (e.statusCode == 401) {
                    throw WrongBasicCredentialException()
                } else if (e.statusCode == 404) {
                    throw WrongAPIURLFormatException()
                }
                throw e
            }

            val userId = user.id ?: return SignInResponse(false, false, null)
            var color = analyseUserProfileColor(user)
            val accountType = SignInActivity.detectAccountType(twitter, user)
            val account = ParcelableAccountUtils.getAccount(activity,
                    UserKey(userId, UserKeyUtils.getUserHost(user)))
            if (account != null) {
                color = account.color
            }
            return SignInResponse(account != null, username, password, user,
                    color, apiUrlFormat, noVersionSuffix, accountType)
        }


        @Throws(MicroBlogException::class)
        private fun authTwipOMode(): SignInResponse {
            val activity = activityRef.get() ?: return SignInResponse(false, false, null)
            val versionSuffix = if (noVersionSuffix) null else "1.1"
            val endpoint = Endpoint(MicroBlogAPIFactory.getApiUrl(apiUrlFormat, "api",
                    versionSuffix))
            val auth = EmptyAuthorization()
            val twitter = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, MicroBlog::class.java)
            val user = twitter.verifyCredentials()
            val userId = user.id ?: return SignInResponse(false, false, null)
            var color = analyseUserProfileColor(user)
            val accountType = SignInActivity.detectAccountType(twitter, user)
            val account = ParcelableAccountUtils.getAccount(activity,
                    UserKey(userId, UserKeyUtils.getUserHost(user)))
            if (account != null) {
                color = account.color
            }
            return SignInResponse(account != null, user, color, apiUrlFormat,
                    noVersionSuffix, accountType)
        }

        @Throws(MicroBlogException::class)
        private fun getOAuthSignInResponse(activity: SignInActivity,
                                           accessToken: OAuthToken,
                                           userId: String, authType: Int): SignInResponse {
            val auth = OAuthAuthorization(consumerKey.oauthToken,
                    consumerKey.oauthTokenSecret, accessToken)
            val endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat,
                    sameOAuthSigningUrl, noVersionSuffix)
            val twitter = MicroBlogAPIFactory.getInstance(activity, endpoint, auth, MicroBlog::class.java)
            val user = twitter.verifyCredentials()
            var color = analyseUserProfileColor(user)
            val accountType = SignInActivity.detectAccountType(twitter, user)
            val account = ParcelableAccountUtils.getAccount(activity,
                    UserKey(userId, UserKeyUtils.getUserHost(user)))
            if (account != null) {
                color = account.color
            }
            return SignInResponse(account != null, auth, user, authType, color,
                    apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix, accountType)
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
                    val activity = activityRef.get() ?: return@Runnable
                    activity.dismissDialogFragment(SignInActivity.FRAGMENT_TAG_SIGN_IN_PROGRESS)
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
                        df.show(sia.supportFragmentManager, null)
                        Unit
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

        val FRAGMENT_TAG_SIGN_IN_PROGRESS = "sign_in_progress"
        private val TWITTER_SIGNUP_URL = "https://twitter.com/signup"
        private val EXTRA_API_LAST_CHANGE = "api_last_change"
        private val DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/"

        internal fun detectAccountType(twitter: MicroBlog, user: User): Pair<String, String> {
            try {
                // Get StatusNet specific resource
                val config = twitter.statusNetConfig
                val extra = StatusNetAccountExtra()
                val site = config.site
                if (site != null) {
                    extra.textLimit = site.textLimit
                }
                return Pair.create<String, String>(ParcelableAccount.Type.STATUSNET,
                        JsonSerializer.serialize(extra, StatusNetAccountExtra::class.java))
            } catch (e: MicroBlogException) {
                // Ignore
            }

            try {
                // Get Twitter official only resource
                val paging = Paging()
                paging.count(1)
                twitter.getActivitiesAboutMe(paging)
                val extra = TwitterAccountExtra()
                extra.setIsOfficialCredentials(true)
                return Pair.create<String, String>(ParcelableAccount.Type.TWITTER,
                        JsonSerializer.serialize(extra, TwitterAccountExtra::class.java))
            } catch (e: MicroBlogException) {
                // Ignore
            }

            if (UserKeyUtils.isFanfouUser(user)) {
                return Pair.create<String, String>(ParcelableAccount.Type.FANFOU, null)
            }
            return Pair.create<String, String>(ParcelableAccount.Type.TWITTER, null)
        }
    }


}
