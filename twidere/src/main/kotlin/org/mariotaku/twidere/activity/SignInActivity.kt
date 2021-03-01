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
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import androidx.loader.app.LoaderManager
import androidx.core.content.ContextCompat
import androidx.loader.content.Loader
import androidx.collection.ArraySet
import androidx.core.view.ViewCompat
import androidx.appcompat.app.AlertDialog
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.view.View.OnClickListener
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.dialog_expandable_list.*
import kotlinx.android.synthetic.main.dialog_login_verification_code.*
import nl.komponents.kovenant.CancelException
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.combine.and
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.*
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.mastodon.MastodonOAuth2
import org.mariotaku.microblog.library.mastodon.annotation.AuthScope
import org.mariotaku.microblog.library.twitter.TwitterOAuth
import org.mariotaku.microblog.library.twitter.auth.BasicAuthorization
import org.mariotaku.microblog.library.twitter.auth.EmptyAuthorization
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.oauth.OAuthToken
import org.mariotaku.restfu.oauth2.OAuth2Authorization
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_API_CONFIG
import org.mariotaku.twidere.constant.apiLastChangeKey
import org.mariotaku.twidere.constant.defaultAPIConfigKey
import org.mariotaku.twidere.constant.randomizeAccountNameKey
import org.mariotaku.twidere.extension.*
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.isFanfouUser
import org.mariotaku.twidere.extension.model.api.key
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.fragment.APIEditorDialogFragment
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.ProgressDialogFragment
import org.mariotaku.twidere.loader.DefaultAPIConfigLoader
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.AccountExtras
import org.mariotaku.twidere.model.account.MastodonAccountExtras
import org.mariotaku.twidere.model.account.StatusNetAccountExtras
import org.mariotaku.twidere.model.account.TwitterAccountExtras
import org.mariotaku.twidere.model.account.cred.*
import org.mariotaku.twidere.model.analyzer.SignIn
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


class SignInActivity : BaseActivity(), OnClickListener, TextWatcher,
        APIEditorDialogFragment.APIEditorCallback {

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


        editUsername.addTextChangedListener(this)
        editPassword.addTextChangedListener(this)

        signIn.setOnClickListener(this)
        signUp.setOnClickListener(this)
        passwordSignIn.setOnClickListener(this)

        val color = ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.material_light_green))
        ViewCompat.setBackgroundTintList(signIn, color)


        if (savedInstanceState != null) {
            apiConfig = savedInstanceState.getParcelable(EXTRA_API_CONFIG)!!
            apiChangeTimestamp = savedInstanceState.getLong(EXTRA_API_LAST_CHANGE)
        } else {
            apiConfig = kPreferences[defaultAPIConfigKey]
        }

        updateSignInType()
        setSignInButton()

        if (savedInstanceState == null) {
            // Only start at the first time
            showLoginTypeChooser()
            // Must call this before cookie manager under lollipop
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                CookieSyncManager.createInstance(this)
            }
            CookieManager.getInstance().removeAllCookiesSupport()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_sign_in, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_EDIT_API -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    data.getParcelableExtra<CustomAPIConfig>(EXTRA_API_CONFIG)?.let {
                        apiConfig = it
                    }
                    updateSignInType()
                }
                setSignInButton()
                invalidateOptionsMenu()
            }
            REQUEST_BROWSER_TWITTER_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    handleBrowserLoginResult(data)
                }
            }
            REQUEST_BROWSER_MASTODON_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val code = data.getStringExtra(EXTRA_CODE)
                    val extras = data.getBundleExtra(EXTRA_EXTRAS)!!
                    val host = extras.getString(EXTRA_HOST)!!
                    val clientId = extras.getString(EXTRA_CLIENT_ID)!!
                    val clientSecret = extras.getString(EXTRA_CLIENT_SECRET)!!

                    if (code != null) {
                        finishMastodonBrowserLogin(host, clientId, clientSecret, code)
                    }
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
                val uri = apiConfig.signUpUrl?.let(Uri::parse) ?: return
                OnLinkClickHandler.openLink(this, preferences, uri)
            }
            signIn -> {
                if (usernamePasswordContainer.visibility != View.VISIBLE) {
                    editUsername.text = null
                    editPassword.text = null
                }
                setDefaultAPI()
                if (apiConfig.type == AccountType.MASTODON) {
                    performMastodonLogin()
                } else when (apiConfig.credentialsType) {
                    Credentials.Type.OAUTH -> {
                        performBrowserLogin()
                    }
                    else -> {
                        val username = editUsername.text.toString()
                        val password = editPassword.text.toString()
                        performUserPassLogin(username, password)
                    }
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


    private fun performBrowserLogin() {
        val weakThis = WeakReference(this)
        executeAfterFragmentResumed { activity ->
            ProgressDialogFragment.show(activity.supportFragmentManager, "get_request_token")
        } and task {
            val activity = weakThis.get() ?: throw InterruptedException()
            val apiConfig = activity.apiConfig
            val apiUrlFormat = apiConfig.apiUrlFormat ?:
                    throw MicroBlogException("Invalid API URL format")
            val endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            val auth = apiConfig.getOAuthAuthorization() ?:
                    throw MicroBlogException("Invalid OAuth credentials")
            val oauth = newMicroBlogInstance(activity, endpoint, auth, apiConfig.type,
                    TwitterOAuth::class.java)
            return@task oauth.getRequestToken(OAUTH_CALLBACK_OOB)
        }.successUi { requestToken ->
            val activity = weakThis.get() ?: return@successUi
            val intent = Intent(activity, BrowserSignInActivity::class.java)
            val apiConfig = activity.apiConfig
            val endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiConfig.apiUrlFormat!!, true)
            intent.data = Uri.parse(endpoint.construct("/oauth/authorize", arrayOf("oauth_token",
                    requestToken.oauthToken)))
            intent.putExtra(EXTRA_EXTRAS, Bundle {
                this[EXTRA_REQUEST_TOKEN] = requestToken.oauthToken
                this[EXTRA_REQUEST_TOKEN_SECRET] = requestToken.oauthTokenSecret
            })
            activity.startActivityForResult(intent, REQUEST_BROWSER_TWITTER_SIGN_IN)
        }.failUi {
            val activity = weakThis.get() ?: return@failUi
            // TODO show error message
            if (it is MicroBlogException) {
                Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
            }
        }.alwaysUi {
            executeAfterFragmentResumed {
                it.supportFragmentManager.dismissDialogFragment("get_request_token")
            }
        }
    }

    private fun performMastodonLogin() {
        val weakThis = WeakReference(this)
        val host = editUsername.string?.takeIf(String::isNotEmpty) ?: run {
            Toast.makeText(this, R.string.message_toast_invalid_mastodon_host,
                    Toast.LENGTH_SHORT).show()
            return
        }
        val scopes = arrayOf(AuthScope.READ, AuthScope.WRITE, AuthScope.FOLLOW)
        executeAfterFragmentResumed { activity ->
            ProgressDialogFragment.show(activity.supportFragmentManager, "open_browser_auth")
        } and task {
            val activity = weakThis.get() ?: throw InterruptedException()
            val registry = activity.mastodonApplicationRegistry
            return@task Pair(host, registry[host] ?: registry.fetch(host, scopes))
        }.successUi { (host, app) ->
            val activity = weakThis.get() ?: return@successUi
            val endpoint = Endpoint("https://$host/")
            val intent = Intent(activity, BrowserSignInActivity::class.java)
            intent.data = Uri.parse(endpoint.construct("/oauth/authorize",
                    arrayOf("response_type", "code"),
                    arrayOf("client_id", app.clientId),
                    arrayOf("redirect_uri", MASTODON_CALLBACK_URL),
                    arrayOf("scope", scopes.joinToString(" "))))
            intent.putExtra(EXTRA_EXTRAS, Bundle {
                this[EXTRA_HOST] = host
                this[EXTRA_CLIENT_ID] = app.clientId
                this[EXTRA_CLIENT_SECRET] = app.clientSecret
            })
            activity.startActivityForResult(intent, REQUEST_BROWSER_MASTODON_SIGN_IN)
        }.failUi {
            val activity = weakThis.get() ?: return@failUi
            // TODO show error message
            activity.onSignInError(it)
        }.alwaysUi {
            val activity = weakThis.get() ?: return@alwaysUi
            activity.executeAfterFragmentResumed {
                it.supportFragmentManager.dismissDialogFragment("open_browser_auth")
            }
        }
    }

    private fun performUserPassLogin(username: String, password: String) {
        if (signInTask != null && signInTask!!.status == AsyncTask.Status.RUNNING) {
            signInTask!!.cancel(true)
        }

        signInTask = SignInTask(this, username, password, apiConfig).apply { execute() }
    }

    private fun onSignInResult(result: SignInResponse) {
        val am = AccountManager.get(this)
        setSignInButton()
        if (result.alreadyLoggedIn) {
            result.updateAccount(am)
            contentResolver.deleteAccountData(result.user.key)
            Toast.makeText(this, R.string.message_toast_already_logged_in, Toast.LENGTH_SHORT).show()
        } else {
            result.addAccount(am, preferences[randomizeAccountNameKey])
            Analyzer.log(SignIn(true, accountType = result.type,
                    credentialsType = apiConfig.credentialsType,
                    officialKey = result.extras?.official == true))
            finishSignIn()
        }
    }

    private fun dismissDialogFragment(tag: String) {
        executeAfterFragmentResumed {
            it.supportFragmentManager.dismissDialogFragment(tag)
        }
    }

    internal fun onSignInError(exception: Exception) {
        DebugLog.w(LOGTAG, "Sign in error", exception)
        var errorReason: String? = null
        when (exception) {
            is AuthenticityTokenException -> {
                Toast.makeText(this, R.string.message_toast_wrong_api_key, Toast.LENGTH_SHORT).show()
                errorReason = "wrong_api_key"
            }
            is WrongUserPassException -> {
                Toast.makeText(this, R.string.message_toast_wrong_username_password, Toast.LENGTH_SHORT).show()
                errorReason = "wrong_username_password"
            }
            is SignInTask.WrongBasicCredentialException -> {
                Toast.makeText(this, R.string.message_toast_wrong_username_password, Toast.LENGTH_SHORT).show()
                errorReason = "wrong_username_password"
            }
            is SignInTask.WrongAPIURLFormatException -> {
                Toast.makeText(this, R.string.message_toast_wrong_api_key, Toast.LENGTH_SHORT).show()
                errorReason = "wrong_api_key"
            }
            is LoginVerificationException -> {
                Toast.makeText(this, R.string.message_toast_login_verification_failed, Toast.LENGTH_SHORT).show()
                errorReason = "login_verification_failed"
            }
            else -> {
                Toast.makeText(this, exception.getErrorMessage(this), Toast.LENGTH_SHORT).show()
            }
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

    internal fun dismissSignInProgressDialog() {
        dismissDialogFragment(FRAGMENT_TAG_SIGN_IN_PROGRESS)
    }

    private fun showLoginTypeChooser() {
        executeAfterFragmentResumed {
            val fm = it.supportFragmentManager
            val df = SignInTypeChooserDialogFragment()
            df.show(fm, "login_type_chooser")
        }
    }


    private fun finishMastodonBrowserLogin(host: String, clientId: String, clientSecret: String, code: String) {
        signInTask = MastodonLoginTask(this, host, clientId, clientSecret, code).apply { execute() }
    }

    private fun handleBrowserLoginResult(intent: Intent?) {
        if (intent == null) return
        val extras = intent.getBundleExtra(EXTRA_EXTRAS) ?: return
        val requestToken = OAuthToken(extras.getString(EXTRA_REQUEST_TOKEN),
                extras.getString(EXTRA_REQUEST_TOKEN_SECRET))
        val verifier = intent.getStringExtra(EXTRA_OAUTH_VERIFIER)
        signInTask = BrowserSignInTask(this, apiConfig, requestToken, verifier).apply { execute() }
    }

    private fun setDefaultAPI() {
        if (!apiConfig.isDefault) return
        val apiLastChange = preferences[apiLastChangeKey]
        if (apiLastChange != apiChangeTimestamp) {
            apiConfig = preferences[defaultAPIConfigKey]
            apiChangeTimestamp = apiLastChange
        }
        updateSignInType()
        setSignInButton()
    }

    private fun updateSignInType() {
        // Mastodon have different case
        if (apiConfig.type == AccountType.MASTODON) {
            usernamePasswordContainer.visibility = View.VISIBLE
            editPassword.visibility = View.GONE
            editUsername.hint = getString(R.string.label_mastodon_host)
        } else when (apiConfig.credentialsType) {
            Credentials.Type.XAUTH, Credentials.Type.BASIC -> {
                usernamePasswordContainer.visibility = View.VISIBLE
                editPassword.visibility = View.VISIBLE
                editUsername.hint = getString(R.string.label_username)
            }
            Credentials.Type.EMPTY -> {
                usernamePasswordContainer.visibility = View.GONE
            }
            else -> {
                usernamePasswordContainer.visibility = View.GONE
            }
        }
    }

    private fun setSignInButton() {
        // Mastodon have different case
        if (apiConfig.type == AccountType.MASTODON) {
            passwordSignIn.visibility = View.GONE
            signIn.isEnabled = true
        } else when (apiConfig.credentialsType) {
            Credentials.Type.XAUTH, Credentials.Type.BASIC -> {
                passwordSignIn.visibility = View.GONE
                signIn.isEnabled = !(editPassword.text.isNullOrEmpty() || editUsername.text.isNullOrEmpty())
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

    class SignInTypeChooserDialogFragment : BaseDialogFragment(),
            LoaderManager.LoaderCallbacks<List<CustomAPIConfig>> {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.dialog_expandable_list)
            val dialog = builder.create()
            dialog.onShow { alertDialog ->
                alertDialog.applyTheme()
                val listView = alertDialog.expandableList
                val adapter = LoginTypeAdapter(requireContext())
                listView.setAdapter(adapter)
                listView.setOnGroupClickListener { _, _, groupPosition, _ ->
                    val type = adapter.getGroup(groupPosition)
                    if (type.hasChildren) return@setOnGroupClickListener false
                    val activity = activity as? SignInActivity
                    val config = type.configs.single()
                    activity?.let {
                        it.apiConfig = config
                        it.updateSignInType()
                        it.setSignInButton()
                    }
                    dismiss()
                    return@setOnGroupClickListener true
                }
                listView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                    val config = adapter.getChild(groupPosition, childPosition)
                    val activity = activity as? SignInActivity
                    activity?.let {
                        it.apiConfig = config
                        it.updateSignInType()
                        it.setSignInButton()
                    }
                    dismiss()
                    return@setOnChildClickListener true
                }

                LoaderManager.getInstance(this).initLoader(0, null, this)
            }
            return dialog
        }

        override fun onLoadFinished(loader: Loader<List<CustomAPIConfig>>, data: List<CustomAPIConfig>) {
            val dialog = dialog ?: return
            val listView: ExpandableListView = dialog.findViewById(R.id.expandableList)
            val defaultConfig = preferences[defaultAPIConfigKey]
            defaultConfig.name = getString(R.string.login_type_user_settings)
            val allConfig = ArraySet(data)
            allConfig.add(defaultConfig)
            val configGroup = allConfig.groupBy { it.safeType }
            val supportedAccountTypes = arrayOf(AccountType.TWITTER, AccountType.FANFOU,
                    AccountType.MASTODON, AccountType.STATUSNET)
            val result = supportedAccountTypes.mapNotNullTo(ArrayList()) { type ->
                if (type == AccountType.MASTODON) return@mapNotNullTo LoginType(type,
                        listOf(CustomAPIConfig.mastodon(requireContext())))
                return@mapNotNullTo configGroup[type]?.let { list ->
                    LoginType(type, list.sortedBy { !it.isDefault })
                }
            }
            (listView.expandableListAdapter as LoginTypeAdapter).data = result
        }

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<CustomAPIConfig>> {
            return DefaultAPIConfigLoader(requireContext())
        }

        override fun onLoaderReset(loader: Loader<List<CustomAPIConfig>>) {

        }

        private data class LoginType(val type: String, val configs: List<CustomAPIConfig>) {
            val hasChildren = configs.size > 1
        }

        private class LoginTypeAdapter(val context: Context) : BaseExpandableListAdapter() {

            private val inflater = LayoutInflater.from(context)

            var data: List<LoginType>? = null

            override fun getGroupCount() = data?.count() ?: 0
            override fun getGroup(groupPosition: Int) = data!![groupPosition]
            override fun getChild(groupPosition: Int, childPosition: Int) =
                    getGroup(groupPosition).configs[childPosition]

            override fun getChildrenCount(groupPosition: Int): Int {
                val size = getGroup(groupPosition).configs.size
                if (size > 1) return size
                return 0
            }

            override fun isChildSelectable(groupPosition: Int, childPosition: Int) = true
            override fun hasStableIds() = false
            override fun getGroupId(groupPosition: Int) = groupPosition.toLong()
            override fun getChildId(groupPosition: Int, childPosition: Int): Long =
                    groupPosition.toLong().shl(32) or childPosition.toLong()


            override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?,
                    parent: ViewGroup): View {
                val view = convertView ?: inflater.inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                val group = getGroup(groupPosition)
                text1.text = APIEditorDialogFragment.getTypeTitle(context, group.type)
                return view
            }

            override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean,
                    convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
                val config = getChild(groupPosition, childPosition)
                val text1 = view.findViewById<TextView>(android.R.id.text1)
                text1.text = config.name
                return view
            }

        }
    }

    internal class InputLoginVerificationDialogFragment : BaseDialogFragment() {

        var deferred: Deferred<String?, Exception>? = null
        var challengeType: String? = null

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.login_verification)
            builder.setView(R.layout.dialog_login_verification_code)
            builder.positive(android.R.string.ok, this::performVerification)
            builder.negative(android.R.string.cancel, this::cancelVerification)
            val dialog = builder.create()
            dialog.onShow(this::onDialogShow)
            return dialog
        }

        override fun onCancel(dialog: DialogInterface) {
            deferred?.reject(CancelException())
        }

        private fun performVerification(dialog: Dialog) {
            deferred?.resolve(dialog.editVerificationCode.string)
        }

        private fun cancelVerification(dialog: Dialog) {
            deferred?.reject(CancelException())
        }

        private fun onDialogShow(dialog: Dialog) {
            (dialog as? AlertDialog)?.applyTheme()
            val verificationHint = dialog.verificationHint
            val editVerification = dialog.editVerificationCode
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
            val builder = AlertDialog.Builder(requireContext())
            builder.setView(R.layout.dialog_password_sign_in)
            builder.positive(R.string.action_sign_in, this::onPositiveButton)
            builder.setNegativeButton(android.R.string.cancel, null)

            val alertDialog = builder.create()
            alertDialog.onShow { dialog ->
                dialog.applyTheme()
                val editUsername = dialog.editUsername
                val editPassword = dialog.editPassword
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        val button = dialog.getButton(DialogInterface.BUTTON_POSITIVE) ?: return
                        button.isEnabled = editUsername.length() > 0 && editPassword.length() > 0
                    }

                    override fun afterTextChanged(s: Editable) {

                    }
                }

                editUsername.addTextChangedListener(textWatcher)
                editPassword.addTextChangedListener(textWatcher)
            }
            return alertDialog
        }

        private fun onPositiveButton(dialog: Dialog) {
            val activity = activity as SignInActivity
            val username = dialog.editUsername.string.orEmpty()
            val password = dialog.editPassword.string.orEmpty()
            activity.setDefaultAPI()
            activity.performUserPassLogin(username, password)
        }
    }

    internal class BrowserSignInTask(activity: SignInActivity, private val apiConfig: CustomAPIConfig,
            private val requestToken: OAuthToken, private val oauthVerifier: String?) :
            AbstractSignInTask(activity) {

        @Throws(Exception::class)
        override fun performLogin(): SignInResponse {
            val context = activityRef.get() ?: throw InterruptedException()
            val versionSuffix = if (apiConfig.isNoVersionSuffix) null else "1.1"
            val apiUrlFormat = apiConfig.apiUrlFormat ?: throw MicroBlogException("No API URL format")
            var auth = apiConfig.getOAuthAuthorization() ?:
                    throw MicroBlogException("Invalid OAuth credential")
            var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            val oauth = newMicroBlogInstance(context, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = TwitterOAuth::class.java)
            val accessToken: OAuthToken
            accessToken = if (oauthVerifier != null) {
                oauth.getAccessToken(requestToken, oauthVerifier)
            } else {
                oauth.getAccessToken(requestToken)
            }
            auth = apiConfig.getOAuthAuthorization(accessToken) ?:
                    throw MicroBlogException("Invalid OAuth credential")
            endpoint = MicroBlogAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", versionSuffix,
                    apiConfig.isSameOAuthUrl)

            val twitter = newMicroBlogInstance(context, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser = twitter.verifyCredentials()
            var color = analyseUserProfileColor(apiUser)
            val (type, extras) = detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = apiUser.key
            val user = apiUser.toParcelable(accountKey, type, profileImageSize = profileImageSize)
            val am = AccountManager.get(context)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = OAuthCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            credentials.same_oauth_signing_url = apiConfig.isSameOAuthUrl

            credentials.consumer_key = auth.consumerKey
            credentials.consumer_secret = auth.consumerSecret
            credentials.access_token = accessToken.oauthToken
            credentials.access_token_secret = accessToken.oauthTokenSecret

            return SignInResponse(account != null, Credentials.Type.OAUTH, credentials, user, color,
                    type, extras)
        }

    }

    internal class MastodonLoginTask(context: SignInActivity, val host: String, val clientId: String,
            val clientSecret: String, val code: String) : AbstractSignInTask(context) {
        @Throws(Exception::class)
        override fun performLogin(): SignInResponse {
            val context = activityRef.get() ?: throw InterruptedException()
            val oauth2 = newMicroBlogInstance(context, Endpoint("https://$host/"),
                    EmptyAuthorization(), AccountType.MASTODON, MastodonOAuth2::class.java)
            val token = oauth2.getToken(clientId, clientSecret, code, MASTODON_CALLBACK_URL)

            val endpoint = Endpoint("https://$host/api/")
            val auth = OAuth2Authorization(token.accessToken)
            val mastodon = newMicroBlogInstance(context, endpoint = endpoint, auth = auth,
                    accountType = AccountType.MASTODON, cls = Mastodon::class.java)
            val apiAccount = mastodon.verifyCredentials()
            var color = 0
            val accountKey = UserKey(apiAccount.id, host)
            val user = apiAccount.toParcelable(accountKey)
            val am = AccountManager.get(context)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = OAuth2Credentials()
            credentials.api_url_format = endpoint.url
            credentials.no_version_suffix = true

            credentials.access_token = token.accessToken
            return SignInResponse(account != null, Credentials.Type.OAUTH2, credentials, user,
                    color, AccountType.MASTODON, getMastodonAccountExtras(mastodon))
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

        @Throws(AuthenticationException::class, MicroBlogException::class)
        private fun authOAuth(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            val endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            val auth = apiConfig.getOAuthAuthorization() ?:
                    throw MicroBlogException("Invalid OAuth credential")
            val oauth = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = TwitterOAuth::class.java)
            val authenticator = OAuthPasswordAuthenticator(oauth,
                    verificationCallback, userAgent)
            val accessToken = authenticator.getOAuthAccessToken(username, password)
            val userId = accessToken.userId
            return getOAuthSignInResponse(activity, accessToken, Credentials.Type.OAUTH)
        }

        @Throws(MicroBlogException::class)
        private fun authxAuth(): SignInResponse {
            val activity = activityRef.get() ?: throw InterruptedException()
            var endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl)
            var auth = apiConfig.getOAuthAuthorization() ?:
                    throw MicroBlogException("Invalid OAuth credential")
            val oauth = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = TwitterOAuth::class.java)
            val accessToken = oauth.getAccessToken(username, password)
            val userId = accessToken.userId ?: run {
                // Trying to fix up userId if accessToken doesn't contain one.
                auth = apiConfig.getOAuthAuthorization(accessToken) ?:
                        throw MicroBlogException("Invalid OAuth credential")
                endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat,
                        apiConfig.isSameOAuthUrl, apiConfig.isNoVersionSuffix)
                val microBlog = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                        accountType = apiConfig.type, cls = MicroBlog::class.java)
                return@run microBlog.verifyCredentials().id
            }
            return getOAuthSignInResponse(activity, accessToken, Credentials.Type.XAUTH)
        }

        @Throws(MicroBlogException::class, AuthenticationException::class)
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

            var color = analyseUserProfileColor(apiUser)
            val (type, extras) = detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = apiUser.key
            val user = apiUser.toParcelable(accountKey, type, profileImageSize = profileImageSize)
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
                    color, type, extras)
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
            var color = analyseUserProfileColor(apiUser)
            val (type, extras) = detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = apiUser.key
            val user = apiUser.toParcelable(accountKey, type, profileImageSize = profileImageSize)
            val am = AccountManager.get(activity)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = EmptyCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            return SignInResponse(account != null, Credentials.Type.EMPTY, credentials, user, color,
                    type, extras)
        }

        @Throws(MicroBlogException::class)
        private fun getOAuthSignInResponse(activity: SignInActivity, accessToken: OAuthToken,
                @Credentials.Type authType: String): SignInResponse {
            val auth = apiConfig.getOAuthAuthorization(accessToken) ?:
                    throw MicroBlogException("Invalid OAuth credential")
            val endpoint = MicroBlogAPIFactory.getOAuthRestEndpoint(apiUrlFormat,
                    apiConfig.isSameOAuthUrl, apiConfig.isNoVersionSuffix)
            val twitter = newMicroBlogInstance(activity, endpoint = endpoint, auth = auth,
                    accountType = apiConfig.type, cls = MicroBlog::class.java)
            val apiUser = twitter.verifyCredentials()
            var color = analyseUserProfileColor(apiUser)
            val (type, extras) = detectAccountType(twitter, apiUser, apiConfig.type)
            val accountKey = apiUser.key
            val user = apiUser.toParcelable(accountKey, type, profileImageSize = profileImageSize)
            val am = AccountManager.get(activity)
            val account = AccountUtils.findByAccountKey(am, accountKey)
            if (account != null) {
                color = account.getColor(am)
            }
            val credentials = OAuthCredentials()
            credentials.api_url_format = apiUrlFormat
            credentials.no_version_suffix = apiConfig.isNoVersionSuffix

            credentials.same_oauth_signing_url = apiConfig.isSameOAuthUrl

            credentials.consumer_key = auth.consumerKey
            credentials.consumer_secret = auth.consumerSecret
            credentials.access_token = accessToken.oauthToken
            credentials.access_token_secret = accessToken.oauthTokenSecret

            return SignInResponse(account != null, authType, credentials, user, color, type, extras)
        }

        internal class WrongBasicCredentialException : AuthenticationException()

        internal class WrongAPIURLFormatException : AuthenticationException()

        internal inner class InputLoginVerificationCallback : LoginVerificationCallback {

            override fun getLoginVerification(challengeType: String): String? {
                // Dismiss current progress dialog
                publishProgress(Runnable {
                    activityRef.get()?.dismissSignInProgressDialog()
                })
                val deferred = deferred<String?, Exception>()
                // Show verification input dialog and wait for user input
                publishProgress(Runnable {
                    val activity = activityRef.get() ?: return@Runnable
                    activity.executeAfterFragmentResumed {
                        val sia = it as SignInActivity
                        val df = InputLoginVerificationDialogFragment()
                        df.isCancelable = false
                        df.deferred = deferred
                        df.challengeType = challengeType
                        df.show(sia.supportFragmentManager, "login_challenge_$challengeType")
                    }
                })

                return try {
                    deferred.promise.get()
                } catch (e: CancelException) {
                    throw MicroBlogException(e)
                } finally {
                    // Show progress dialog
                    publishProgress(Runnable {
                        activityRef.get()?.showSignInProgressDialog()
                    })
                }
            }

        }

    }

    internal abstract class AbstractSignInTask(activity: SignInActivity) : AsyncTask<Any, Runnable, SingleResponse<SignInResponse>>() {
        protected val activityRef = WeakReference(activity)

        protected val profileImageSize: String = activity.getString(R.string.profile_image_size)

        final override fun doInBackground(vararg args: Any?): SingleResponse<SignInResponse> {
            return try {
                SingleResponse.getInstance(performLogin())
            } catch (e: Exception) {
                SingleResponse.getInstance(e)
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

    internal data class SignInResponse(
            val alreadyLoggedIn: Boolean,
            @Credentials.Type val credsType: String = Credentials.Type.EMPTY,
            val credentials: Credentials,
            val user: ParcelableUser,
            val color: Int = 0,
            val type: String,
            val extras: AccountExtras?
    ) {

        private fun writeAccountInfo(action: (k: String, v: String?) -> Unit) {
            action(ACCOUNT_USER_DATA_KEY, user.key.toString())
            action(ACCOUNT_USER_DATA_TYPE, type)
            action(ACCOUNT_USER_DATA_CREDS_TYPE, credsType)

            action(ACCOUNT_USER_DATA_ACTIVATED, true.toString())
            action(ACCOUNT_USER_DATA_COLOR, toHexColor(color, format = HexColorFormat.RGB))

            action(ACCOUNT_USER_DATA_USER, JsonSerializer.serialize(user))
            action(ACCOUNT_USER_DATA_EXTRAS, extras?.let { JsonSerializer.serialize(it) })
        }

        private fun writeAuthToken(am: AccountManager, account: Account) {
            val authToken = JsonSerializer.serialize(credentials)
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

    companion object {
        const val REQUEST_BROWSER_TWITTER_SIGN_IN = 101
        const val REQUEST_BROWSER_MASTODON_SIGN_IN = 102

        private const val FRAGMENT_TAG_SIGN_IN_PROGRESS = "sign_in_progress"
        private const val EXTRA_API_LAST_CHANGE = "api_last_change"

        @Throws(IOException::class)
        internal fun detectAccountType(twitter: MicroBlog, user: User, type: String?): Pair<String, AccountExtras?> {
            when (type) {
                AccountType.STATUSNET -> {
                    return Pair(type, getStatusNetAccountExtras(twitter))
                }
                AccountType.TWITTER -> {
                    return Pair(type, getTwitterAccountExtras(twitter))
                }
                AccountType.FANFOU -> {
                    return Pair(AccountType.FANFOU, null)
                }
                else -> {
                    if (user.isFanfouUser) {
                        return Pair(AccountType.FANFOU, null)
                    }
                }
            }
            return Pair(AccountType.TWITTER, null)
        }

        private fun getStatusNetAccountExtras(twitter: MicroBlog): StatusNetAccountExtras {
            // Get StatusNet specific resource
            val config = twitter.statusNetConfig
            return StatusNetAccountExtras().apply {
                textLimit = config.site?.textLimit ?: -1
                uploadLimit = config.attachments?.fileQuota ?: -1L
            }
        }

        private fun getTwitterAccountExtras(twitter: MicroBlog): TwitterAccountExtras {
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
            return extras
        }

        private fun getMastodonAccountExtras(mastodon: Mastodon): MastodonAccountExtras {
            return MastodonAccountExtras()
        }

        private val CustomAPIConfig.signUpUrlOrDefault: String?
            get() = signUpUrl ?: when (type) {
                AccountType.TWITTER -> "https://twitter.com/signup"
                AccountType.FANFOU -> "https://fanfou.com/register"
                else -> null
            }

    }

}
