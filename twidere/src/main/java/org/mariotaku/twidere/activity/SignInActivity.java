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

package org.mariotaku.twidere.activity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IExtendedActivity;
import org.mariotaku.twidere.api.statusnet.model.StatusNetConfig;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterOAuth;
import org.mariotaku.twidere.api.twitter.auth.BasicAuthorization;
import org.mariotaku.twidere.api.twitter.auth.EmptyAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.fragment.BaseSupportDialogFragment;
import org.mariotaku.twidere.fragment.SupportProgressDialogFragment;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.StatusNetAccountExtra;
import org.mariotaku.twidere.model.TwitterAccountExtra;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableUserUtils;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.DataStoreUtils;
import org.mariotaku.twidere.util.JsonSerializer;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.AuthenticationException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.AuthenticityTokenException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.LoginVerificationException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.WrongUserPassException;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.TwitterContentUtils;
import org.mariotaku.twidere.util.UserAgentUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator;

import java.lang.ref.WeakReference;

public class SignInActivity extends BaseActivity implements OnClickListener, TextWatcher {

    public static final String FRAGMENT_TAG_SIGN_IN_PROGRESS = "sign_in_progress";
    private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";
    private static final String EXTRA_API_LAST_CHANGE = "api_last_change";
    private static final String DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/";
    @Nullable
    private String mAPIUrlFormat;
    private int mAuthType;
    private String mConsumerKey, mConsumerSecret;
    private String mUsername, mPassword;
    private long mAPIChangeTimestamp;
    private boolean mSameOAuthSigningUrl, mNoVersionSuffix;
    private EditText mEditUsername, mEditPassword;
    private Button mSignInButton, mSignUpButton;
    private LinearLayout mSignInSignUpContainer, mUsernamePasswordContainer;
    private ContentResolver mResolver;
    private AbstractSignInTask mTask;

    @Override
    public void afterTextChanged(final Editable s) {

    }

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_EDIT_API: {
                if (resultCode == RESULT_OK) {
                    mAPIUrlFormat = data.getStringExtra(Accounts.API_URL_FORMAT);
                    mAuthType = data.getIntExtra(Accounts.AUTH_TYPE, ParcelableCredentials.AUTH_TYPE_OAUTH);
                    mSameOAuthSigningUrl = data.getBooleanExtra(Accounts.SAME_OAUTH_SIGNING_URL, false);
                    mNoVersionSuffix = data.getBooleanExtra(Accounts.NO_VERSION_SUFFIX, false);
                    mConsumerKey = data.getStringExtra(Accounts.CONSUMER_KEY);
                    mConsumerSecret = data.getStringExtra(Accounts.CONSUMER_SECRET);
                    final boolean isTwipOMode = mAuthType == ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE;
                    mUsernamePasswordContainer.setVisibility(isTwipOMode ? View.GONE : View.VISIBLE);
                    mSignInSignUpContainer.setOrientation(isTwipOMode ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
                }
                setSignInButton();
                invalidateOptionsMenu();
                break;
            }
            case REQUEST_BROWSER_SIGN_IN: {
                if (resultCode == BaseActivity.RESULT_OK && data != null) {
                    doBrowserLogin(data);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.sign_up: {
                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(TWITTER_SIGNUP_URL));
                startActivity(intent);
                break;
            }
            case R.id.sign_in: {
                doLogin();
                break;
            }
            case R.id.sign_in_method_introduction: {
                final FragmentManager fm = getSupportFragmentManager();
                new SignInMethodIntroductionDialogFragment().show(fm.beginTransaction(),
                        "sign_in_method_introduction");
                break;
            }
        }
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mEditUsername = (EditText) findViewById(R.id.username);
        mEditPassword = (EditText) findViewById(R.id.password);
        mSignInButton = (Button) findViewById(R.id.sign_in);
        mSignUpButton = (Button) findViewById(R.id.sign_up);
        mSignInSignUpContainer = (LinearLayout) findViewById(R.id.sign_in_sign_up);
        mUsernamePasswordContainer = (LinearLayout) findViewById(R.id.username_password);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        getLoaderManager().destroyLoader(0);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                final UserKey[] accountKeys = DataStoreUtils.getActivatedAccountKeys(this);
                if (accountKeys.length > 0) {
                    onBackPressed();
                }
                break;
            }
            case R.id.settings: {
                if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
                    return false;
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.edit_api: {
                if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
                    return false;
                setDefaultAPI();
                final Intent intent = new Intent(this, APIEditorActivity.class);
                intent.putExtra(Accounts.API_URL_FORMAT, mAPIUrlFormat);
                intent.putExtra(Accounts.AUTH_TYPE, mAuthType);
                intent.putExtra(Accounts.SAME_OAUTH_SIGNING_URL, mSameOAuthSigningUrl);
                intent.putExtra(Accounts.NO_VERSION_SUFFIX, mNoVersionSuffix);
                intent.putExtra(Accounts.CONSUMER_KEY, mConsumerKey);
                intent.putExtra(Accounts.CONSUMER_SECRET, mConsumerSecret);
                startActivityForResult(intent, REQUEST_EDIT_API);
                break;
            }
            case R.id.open_in_browser: {
                if (mAuthType != ParcelableCredentials.AUTH_TYPE_OAUTH || mTask != null
                        && mTask.getStatus() == AsyncTask.Status.RUNNING) return false;
                saveEditedText();
                final Intent intent = new Intent(this, BrowserSignInActivity.class);
                intent.putExtra(Accounts.CONSUMER_KEY, mConsumerKey);
                intent.putExtra(Accounts.CONSUMER_SECRET, mConsumerSecret);
                intent.putExtra(Accounts.API_URL_FORMAT, mAPIUrlFormat);
                startActivityForResult(intent, REQUEST_BROWSER_SIGN_IN);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        final MenuItem itemBrowser = menu.findItem(R.id.open_in_browser);
        if (itemBrowser != null) {
            final boolean is_oauth = mAuthType == ParcelableCredentials.AUTH_TYPE_OAUTH;
            itemBrowser.setVisible(is_oauth);
            itemBrowser.setEnabled(is_oauth);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        saveEditedText();
        setDefaultAPI();
        outState.putString(Accounts.API_URL_FORMAT, mAPIUrlFormat);
        outState.putInt(Accounts.AUTH_TYPE, mAuthType);
        outState.putBoolean(Accounts.SAME_OAUTH_SIGNING_URL, mSameOAuthSigningUrl);
        outState.putBoolean(Accounts.NO_VERSION_SUFFIX, mNoVersionSuffix);
        outState.putString(Accounts.CONSUMER_KEY, mConsumerKey);
        outState.putString(Accounts.CONSUMER_SECRET, mConsumerSecret);
        outState.putString(Accounts.SCREEN_NAME, mUsername);
        outState.putString(Accounts.PASSWORD, mPassword);
        outState.putLong(EXTRA_API_LAST_CHANGE, mAPIChangeTimestamp);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
        setSignInButton();
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResolver = getContentResolver();
        setContentView(R.layout.activity_sign_in);

        if (savedInstanceState != null) {
            mAPIUrlFormat = savedInstanceState.getString(Accounts.API_URL_FORMAT);
            mAuthType = savedInstanceState.getInt(Accounts.AUTH_TYPE);
            mSameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL);
            mConsumerKey = Utils.trim(savedInstanceState.getString(Accounts.CONSUMER_KEY));
            mConsumerSecret = Utils.trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET));
            mUsername = savedInstanceState.getString(Accounts.SCREEN_NAME);
            mPassword = savedInstanceState.getString(Accounts.PASSWORD);
            mAPIChangeTimestamp = savedInstanceState.getLong(EXTRA_API_LAST_CHANGE);
        }

        final boolean isTwipOMode = mAuthType == ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE;
        mUsernamePasswordContainer.setVisibility(isTwipOMode ? View.GONE : View.VISIBLE);
        mSignInSignUpContainer.setOrientation(isTwipOMode ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);

        mEditUsername.setText(mUsername);
        mEditUsername.addTextChangedListener(this);
        mEditPassword.setText(mPassword);
        mEditPassword.addTextChangedListener(this);

        mSignUpButton.setOnClickListener(this);

        final ColorStateList color = ColorStateList.valueOf(ContextCompat.getColor(this,
                R.color.material_light_green));
        ViewCompat.setBackgroundTintList(mSignInButton, color);
        setSignInButton();

        final String consumerKey = mPreferences.getString(KEY_CONSUMER_KEY, null);
        final String consumerSecret = mPreferences.getString(KEY_CONSUMER_SECRET, null);
        if (BuildConfig.SHOW_CUSTOM_TOKEN_DIALOG && savedInstanceState == null &&
                !mPreferences.getBoolean(KEY_CONSUMER_KEY_SECRET_SET, false) &&
                !Utils.isCustomConsumerKeySecret(consumerKey, consumerSecret)) {
            final SetConsumerKeySecretDialogFragment df = new SetConsumerKeySecretDialogFragment();
            df.setCancelable(false);
            df.show(getSupportFragmentManager(), "set_consumer_key_secret");
        }

    }

    private void doLogin() {
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
        saveEditedText();
        setDefaultAPI();
        final OAuthToken consumerKey = TwitterAPIFactory.getOAuthToken(mConsumerKey, mConsumerSecret);
        final String apiUrlFormat = TextUtils.isEmpty(mAPIUrlFormat) ? DEFAULT_TWITTER_API_URL_FORMAT : mAPIUrlFormat;
        mTask = new SignInTask(this, mUsername, mPassword, mAuthType, consumerKey, apiUrlFormat,
                mSameOAuthSigningUrl, mNoVersionSuffix);
        AsyncTaskUtils.executeTask(mTask);
    }

    private void doBrowserLogin(final Intent intent) {
        if (intent == null) return;
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            mTask.cancel(true);
        }
        saveEditedText();
        setDefaultAPI();
        final String verifier = intent.getStringExtra(EXTRA_OAUTH_VERIFIER);
        final OAuthToken consumerKey = TwitterAPIFactory.getOAuthToken(mConsumerKey, mConsumerSecret);
        final OAuthToken requestToken = new OAuthToken(intent.getStringExtra(EXTRA_REQUEST_TOKEN),
                intent.getStringExtra(EXTRA_REQUEST_TOKEN_SECRET));
        final String apiUrlFormat = TextUtils.isEmpty(mAPIUrlFormat) ? DEFAULT_TWITTER_API_URL_FORMAT : mAPIUrlFormat;
        mTask = new BrowserSignInTask(this, consumerKey, requestToken, verifier, apiUrlFormat,
                mSameOAuthSigningUrl, mNoVersionSuffix);
        AsyncTaskUtils.executeTask(mTask);
    }


    private void saveEditedText() {
        if (mEditUsername == null || mEditPassword == null) return;
        mUsername = ParseUtils.parseString(mEditUsername.getText());
        mPassword = ParseUtils.parseString(mEditPassword.getText());
    }

    private void setDefaultAPI() {
        final long apiLastChange = mPreferences.getLong(KEY_API_LAST_CHANGE, mAPIChangeTimestamp);
        final boolean defaultApiChanged = apiLastChange != mAPIChangeTimestamp;
        final String apiUrlFormat = Utils.getNonEmptyString(mPreferences, KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT);
        final int authType = mPreferences.getInt(KEY_AUTH_TYPE, ParcelableCredentials.AUTH_TYPE_OAUTH);
        final boolean sameOAuthSigningUrl = mPreferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false);
        final boolean noVersionSuffix = mPreferences.getBoolean(KEY_NO_VERSION_SUFFIX, false);
        final String consumerKey = Utils.getNonEmptyString(mPreferences, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
        final String consumerSecret = Utils.getNonEmptyString(mPreferences, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
        if (TextUtils.isEmpty(mAPIUrlFormat) || defaultApiChanged) {
            mAPIUrlFormat = apiUrlFormat;
        }
        if (defaultApiChanged) {
            mAuthType = authType;
        }
        if (defaultApiChanged) {
            mSameOAuthSigningUrl = sameOAuthSigningUrl;
        }
        if (defaultApiChanged) {
            mNoVersionSuffix = noVersionSuffix;
        }
        if (TextUtils.isEmpty(mConsumerKey) || defaultApiChanged) {
            mConsumerKey = consumerKey;
        }
        if (TextUtils.isEmpty(mConsumerSecret) || defaultApiChanged) {
            mConsumerSecret = consumerSecret;
        }
        if (defaultApiChanged) {
            mAPIChangeTimestamp = apiLastChange;
        }
    }

    private void setSignInButton() {
        mSignInButton.setEnabled(mEditPassword.getText().length() > 0 && mEditUsername.getText().length() > 0
                || mAuthType == ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE);
    }

    void onSignInResult(final SignInResponse result) {
        dismissDialogFragment(FRAGMENT_TAG_SIGN_IN_PROGRESS);
        if (result != null) {
            if (result.alreadyLoggedIn) {
                final ContentValues values = result.toContentValues();
                if (values != null) {
                    final String where = Expression.equalsArgs(Accounts.ACCOUNT_KEY).getSQL();
                    final String[] whereArgs = {values.getAsString(Accounts.ACCOUNT_KEY)};
                    mResolver.update(Accounts.CONTENT_URI, values, where, whereArgs);
                }
                Toast.makeText(this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show();
            } else if (result.succeed) {
                final ContentValues values = result.toContentValues();
                if (values != null) {
                    mResolver.insert(Accounts.CONTENT_URI, values);
                }
                final Intent intent = new Intent(this, HomeActivity.class);
                //TODO refresh timelines
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } else {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, result.exception);
                }
                if (result.exception instanceof AuthenticityTokenException) {
                    Toast.makeText(this, R.string.wrong_api_key, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof WrongUserPassException) {
                    Toast.makeText(this, R.string.wrong_username_password, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof SignInTask.WrongBasicCredentialException) {
                    Toast.makeText(this, R.string.wrong_username_password, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof SignInTask.WrongAPIURLFormatException) {
                    Toast.makeText(this, R.string.wrong_api_key, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof LoginVerificationException) {
                    Toast.makeText(this, R.string.login_verification_failed, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof AuthenticationException) {
                    Utils.showErrorMessage(this, getString(R.string.action_signing_in), result.exception.getCause(), true);
                } else {
                    Utils.showErrorMessage(this, getString(R.string.action_signing_in), result.exception, true);
                }
            }
        }
        setSignInButton();
    }


    void dismissDialogFragment(final String tag) {
        executeAfterFragmentResumed(new Action() {
            @Override
            public void execute(IExtendedActivity activity) {
                final FragmentManager fm = getSupportFragmentManager();
                final Fragment f = fm.findFragmentByTag(tag);
                if (f instanceof DialogFragment) {
                    ((DialogFragment) f).dismiss();
                }
            }
        });
    }

    void onSignInStart() {
        showSignInProgressDialog();
    }

    void showSignInProgressDialog() {
        executeAfterFragmentResumed(new Action() {
            @Override
            public void execute(IExtendedActivity activity) {
                if (isFinishing()) return;
                final FragmentManager fm = getSupportFragmentManager();
                final FragmentTransaction ft = fm.beginTransaction();
                final SupportProgressDialogFragment fragment = new SupportProgressDialogFragment();
                fragment.setCancelable(false);
                fragment.show(ft, FRAGMENT_TAG_SIGN_IN_PROGRESS);
            }
        });
    }

    @Nullable
    private static Pair<String, String> detectAccountType(Twitter twitter, User user) {
        try {
            // Get StatusNet specific resource
            StatusNetConfig config = twitter.getStatusNetConfig();
            StatusNetAccountExtra extra = new StatusNetAccountExtra();
            final StatusNetConfig.Site site = config.getSite();
            if (site != null) {
                extra.setTextLimit(site.getTextLimit());
            }
            return Pair.create(ParcelableAccount.Type.STATUSNET,
                    JsonSerializer.serialize(extra, StatusNetAccountExtra.class));
        } catch (TwitterException e) {
            // Ignore
        }
        try {
            // Get Twitter official only resource
            Paging paging = new Paging();
            paging.count(1);
            twitter.getActivitiesAboutMe(paging);
            TwitterAccountExtra extra = new TwitterAccountExtra();
            extra.setIsOfficialCredentials(true);
            return Pair.create(ParcelableAccount.Type.TWITTER,
                    JsonSerializer.serialize(extra, TwitterAccountExtra.class));
        } catch (TwitterException e) {
            // Ignore
        }
        if (UserKeyUtils.isFanfouUser(user)) {
            return Pair.create(ParcelableAccount.Type.FANFOU, null);
        }
        return Pair.create(ParcelableAccount.Type.TWITTER, null);
    }

    public static abstract class AbstractSignInTask extends AsyncTask<Object, Runnable, SignInResponse> {

        protected final WeakReference<SignInActivity> activityRef;

        public AbstractSignInTask(final SignInActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPostExecute(final SignInResponse result) {
            final SignInActivity activity = activityRef.get();
            if (activity != null) {
                activity.onSignInResult(result);
            }
        }

        @Override
        protected void onPreExecute() {
            final SignInActivity activity = activityRef.get();
            if (activity != null) {
                activity.onSignInStart();
            }
        }

        @Override
        protected void onProgressUpdate(Runnable... values) {
            for (Runnable value : values) {
                value.run();
            }
        }

        int analyseUserProfileColor(final User user) throws TwitterException {
            if (user == null) throw new TwitterException("Unable to get user info");
            return ParseUtils.parseColor("#" + user.getProfileLinkColor(), Color.TRANSPARENT);
        }

    }

    public static class BrowserSignInTask extends AbstractSignInTask {

        private final Context context;

        @NonNull
        private final String apiUrlFormat;
        @Nullable
        private final String oauthVerifier;
        private final OAuthToken consumerKey, requestToken;
        private final boolean sameOauthSigningUrl, noVersionSuffix;

        public BrowserSignInTask(@NonNull final SignInActivity context,
                                 @NonNull final OAuthToken consumerKey,
                                 @NonNull final OAuthToken requestToken,
                                 @Nullable final String oauthVerifier,
                                 @NonNull final String apiUrlFormat,
                                 final boolean sameOauthSigningUrl, final boolean noVersionSuffix) {
            super(context);
            this.context = context;
            this.consumerKey = consumerKey;
            this.requestToken = requestToken;
            this.oauthVerifier = oauthVerifier;
            this.apiUrlFormat = apiUrlFormat;
            this.sameOauthSigningUrl = sameOauthSigningUrl;
            this.noVersionSuffix = noVersionSuffix;
        }

        @Override
        protected SignInResponse doInBackground(final Object... params) {
            try {
                final String versionSuffix = noVersionSuffix ? null : "1.1";
                Endpoint endpoint = TwitterAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                        sameOauthSigningUrl);
                final TwitterOAuth oauth = TwitterAPIFactory.getInstance(context, endpoint,
                        new OAuthAuthorization(consumerKey.getOauthToken(),
                                consumerKey.getOauthTokenSecret()), TwitterOAuth.class);
                final OAuthToken accessToken;
                if (oauthVerifier != null) {
                    accessToken = oauth.getAccessToken(requestToken, oauthVerifier);
                } else {
                    accessToken = oauth.getAccessToken(requestToken);
                }
                final OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(),
                        consumerKey.getOauthTokenSecret(), accessToken);
                endpoint = TwitterAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", versionSuffix,
                        sameOauthSigningUrl);
                final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint, auth,
                        Twitter.class);
                final User user = twitter.verifyCredentials();
                final int color = analyseUserProfileColor(user);
                return new SignInResponse(Utils.isUserLoggedIn(context, user.getId()), auth, user,
                        ParcelableCredentials.AUTH_TYPE_OAUTH, color, apiUrlFormat, sameOauthSigningUrl,
                        noVersionSuffix, detectAccountType(twitter, user));
            } catch (final TwitterException e) {
                return new SignInResponse(false, false, e);
            }
        }
    }

    public static class SignInMethodIntroductionDialogFragment extends BaseSupportDialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(final Bundle savedInstanceState) {
            final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
            final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
            builder.setTitle(R.string.sign_in_method_introduction_title);
            builder.setMessage(R.string.sign_in_method_introduction);
            builder.setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }

    }

    public static class SignInTask extends AbstractSignInTask {

        private final String username, password;
        private final int authType;

        @NonNull
        private final String apiUrlFormat;
        private final boolean sameOAuthSigningUrl, noVersionSuffix;
        private final OAuthToken consumerKey;
        private final InputLoginVerificationCallback verificationCallback;
        private final String userAgent;

        public SignInTask(final SignInActivity activity, final String username, final String password, final int authType,
                          final OAuthToken consumerKey, @NonNull final String apiUrlFormat, final boolean sameOAuthSigningUrl,
                          final boolean noVersionSuffix) {
            super(activity);
            this.username = username;
            this.password = password;
            this.authType = authType;
            this.consumerKey = consumerKey;
            this.apiUrlFormat = apiUrlFormat;
            this.sameOAuthSigningUrl = sameOAuthSigningUrl;
            this.noVersionSuffix = noVersionSuffix;
            verificationCallback = new InputLoginVerificationCallback();
            userAgent = UserAgentUtils.getDefaultUserAgentString(activity);
        }

        @Override
        protected SignInResponse doInBackground(final Object... params) {
            try {
                switch (authType) {
                    case ParcelableCredentials.AUTH_TYPE_OAUTH:
                        return authOAuth();
                    case ParcelableCredentials.AUTH_TYPE_XAUTH:
                        return authxAuth();
                    case ParcelableCredentials.AUTH_TYPE_BASIC:
                        return authBasic();
                    case ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE:
                        return authTwipOMode();
                }
                return authOAuth();
            } catch (final TwitterException e) {
                Log.w(LOGTAG, e);
                return new SignInResponse(false, false, e);
            } catch (final AuthenticationException e) {
                Log.w(LOGTAG, e);
                return new SignInResponse(false, false, e);
            }
        }

        private SignInResponse authOAuth() throws AuthenticationException, TwitterException {
            final SignInActivity activity = activityRef.get();
            if (activity == null) return new SignInResponse(false, false, null);
            Endpoint endpoint = TwitterAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    sameOAuthSigningUrl);
            OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret());
            final TwitterOAuth oauth = TwitterAPIFactory.getInstance(activity, endpoint, auth, TwitterOAuth.class);
            final OAuthPasswordAuthenticator authenticator = new OAuthPasswordAuthenticator(oauth, verificationCallback, userAgent);
            final OAuthToken accessToken = authenticator.getOAuthAccessToken(username, password);
            final String userId = accessToken.getUserId();
            if (userId == null) return new SignInResponse(false, false, null);
            return getOAuthSignInResponse(activity, accessToken, userId,
                    ParcelableCredentials.AUTH_TYPE_OAUTH);
        }

        private SignInResponse authxAuth() throws TwitterException {
            final SignInActivity activity = activityRef.get();
            if (activity == null) return new SignInResponse(false, false, null);
            Endpoint endpoint = TwitterAPIFactory.getOAuthSignInEndpoint(apiUrlFormat,
                    sameOAuthSigningUrl);
            OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret());
            final TwitterOAuth oauth = TwitterAPIFactory.getInstance(activity, endpoint, auth, TwitterOAuth.class);
            final OAuthToken accessToken = oauth.getAccessToken(username, password);
            final String userId = accessToken.getUserId();
            if (userId == null) return new SignInResponse(false, false, null);
            return getOAuthSignInResponse(activity, accessToken, userId,
                    ParcelableCredentials.AUTH_TYPE_XAUTH);
        }

        private SignInResponse authBasic() throws TwitterException, AuthenticationException {
            final SignInActivity activity = activityRef.get();
            if (activity == null) return new SignInResponse(false, false, null);
            final String versionSuffix = noVersionSuffix ? null : "1.1";
            final Endpoint endpoint = new Endpoint(TwitterAPIFactory.getApiUrl(apiUrlFormat, "api", versionSuffix));
            final Authorization auth = new BasicAuthorization(username, password);
            final Twitter twitter = TwitterAPIFactory.getInstance(activity, endpoint, auth,
                    Twitter.class);
            User user;
            try {
                user = twitter.verifyCredentials();
            } catch (TwitterException e) {
                if (e.getStatusCode() == 401) {
                    throw new WrongBasicCredentialException();
                } else if (e.getStatusCode() == 404) {
                    throw new WrongAPIURLFormatException();
                }
                throw e;
            }
            final String userId = user.getId();
            if (userId == null) return new SignInResponse(false, false, null);
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(Utils.isUserLoggedIn(activity, userId), username, password, user,
                    color, apiUrlFormat, noVersionSuffix, detectAccountType(twitter, user));
        }


        private SignInResponse authTwipOMode() throws TwitterException {
            final SignInActivity activity = activityRef.get();
            if (activity == null) return new SignInResponse(false, false, null);
            final String versionSuffix = noVersionSuffix ? null : "1.1";
            final Endpoint endpoint = new Endpoint(TwitterAPIFactory.getApiUrl(apiUrlFormat, "api", versionSuffix));
            final Authorization auth = new EmptyAuthorization();
            final Twitter twitter = TwitterAPIFactory.getInstance(activity, endpoint, auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final String userId = user.getId();
            if (userId == null) return new SignInResponse(false, false, null);
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(Utils.isUserLoggedIn(activity, userId), user, color, apiUrlFormat,
                    noVersionSuffix, detectAccountType(twitter, user));
        }

        private SignInResponse getOAuthSignInResponse(SignInActivity activity, OAuthToken accessToken,
                                                      String userId, int authType) throws TwitterException {
            final OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret(), accessToken);
            final Endpoint endpoint = TwitterAPIFactory.getOAuthRestEndpoint(apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix);
            final Twitter twitter = TwitterAPIFactory.getInstance(activity, endpoint, auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(Utils.isUserLoggedIn(activity, userId), auth, user, authType, color,
                    apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix, detectAccountType(twitter, user));
        }

        static class WrongBasicCredentialException extends AuthenticationException {

        }

        static class WrongAPIURLFormatException extends AuthenticationException {

        }

        class InputLoginVerificationCallback implements OAuthPasswordAuthenticator.LoginVerificationCallback {

            boolean isChallengeFinished;
            String challengeResponse;

            @Override
            public String getLoginVerification(final String challengeType) {
                // Dismiss current progress dialog
                publishProgress(new Runnable() {
                    @Override
                    public void run() {
                        final SignInActivity activity = activityRef.get();
                        if (activity == null) return;
                        activity.dismissDialogFragment(FRAGMENT_TAG_SIGN_IN_PROGRESS);
                    }
                });
                // Show verification input dialog and wait for user input
                publishProgress(new Runnable() {
                    @Override
                    public void run() {
                        final SignInActivity activity = activityRef.get();
                        if (activity == null) return;
                        activity.executeAfterFragmentResumed(new Action() {
                            @Override
                            public void execute(IExtendedActivity activity) {
                                final SignInActivity sia = (SignInActivity) activity;
                                final InputLoginVerificationDialogFragment df =
                                        new InputLoginVerificationDialogFragment();
                                df.setCancelable(false);
                                df.setCallback(InputLoginVerificationCallback.this);
                                df.setChallengeType(challengeType);
                                df.show(sia.getSupportFragmentManager(), null);
                            }
                        });
                    }
                });
                while (!isChallengeFinished) {
                    // Wait for 50ms
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }
                // Show progress dialog
                publishProgress(new Runnable() {
                    @Override
                    public void run() {
                        final SignInActivity activity = activityRef.get();
                        if (activity == null) return;
                        activity.showSignInProgressDialog();
                    }
                });
                return challengeResponse;
            }

            public void setChallengeResponse(String challengeResponse) {
                isChallengeFinished = true;
                this.challengeResponse = challengeResponse;
            }


        }

    }

    public static class InputLoginVerificationDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnShowListener {

        private SignInTask.InputLoginVerificationCallback callback;
        private String challengeType;

        public void setCallback(SignInTask.InputLoginVerificationCallback callback) {
            this.callback = callback;
        }


        @Override
        public void onCancel(DialogInterface dialog) {
            callback.setChallengeResponse(null);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.login_verification);
            builder.setView(R.layout.dialog_login_verification_code);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(this);
            return dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    final AlertDialog alertDialog = (AlertDialog) dialog;
                    final EditText editVerification = (EditText) alertDialog.findViewById(R.id.edit_verification_code);
                    assert editVerification != null;
                    callback.setChallengeResponse(ParseUtils.parseString(editVerification.getText()));
                    break;
                }
                case DialogInterface.BUTTON_NEGATIVE: {
                    callback.setChallengeResponse(null);
                    break;
                }
            }
        }

        public void setChallengeType(String challengeType) {
            this.challengeType = challengeType;
        }

        @Override
        public void onShow(DialogInterface dialog) {
            final AlertDialog alertDialog = (AlertDialog) dialog;
            final TextView verificationHint = (TextView) alertDialog.findViewById(R.id.verification_hint);
            final EditText editVerification = (EditText) alertDialog.findViewById(R.id.edit_verification_code);
            if (verificationHint == null || editVerification == null) return;
            if ("Push".equalsIgnoreCase(challengeType)) {
                verificationHint.setText(R.string.login_verification_push_hint);
                editVerification.setVisibility(View.GONE);
            } else if ("RetypePhoneNumber".equalsIgnoreCase(challengeType)) {
                verificationHint.setText(R.string.login_challenge_retype_phone_hint);
                editVerification.setInputType(InputType.TYPE_CLASS_PHONE);
                editVerification.setVisibility(View.VISIBLE);
            } else if ("RetypeEmail".equalsIgnoreCase(challengeType)) {
                verificationHint.setText(R.string.login_challenge_retype_email_hint);
                editVerification.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                editVerification.setVisibility(View.VISIBLE);
            } else if ("Sms".equalsIgnoreCase(challengeType)) {
                verificationHint.setText(R.string.login_verification_pin_hint);
                editVerification.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                editVerification.setVisibility(View.VISIBLE);
            } else {
                verificationHint.setText(getString(R.string.unsupported_login_verification_type_name,
                        challengeType));
                editVerification.setVisibility(View.VISIBLE);
            }
        }
    }

    static class SignInResponse {

        public final boolean alreadyLoggedIn, succeed;
        public final Exception exception;
        public final String basicUsername, basicPassword;
        public final OAuthAuthorization oauth;
        public final User user;
        public final int authType, color;
        public final String apiUrlFormat;
        public final boolean sameOAuthSigningUrl, noVersionSuffix;
        public final Pair<String, String> accountType;

        public SignInResponse(final boolean alreadyLoggedIn, final boolean succeed,
                              final Exception exception) {
            this(alreadyLoggedIn, succeed, exception, null, null, null, null, 0, 0, null, false,
                    false, null);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final boolean succeed,
                              final Exception exception, final String basicUsername,
                              final String basicPassword, final OAuthAuthorization oauth,
                              final User user, final int authType, final int color,
                              final String apiUrlFormat, final boolean sameOAuthSigningUrl,
                              final boolean noVersionSuffix, final Pair<String, String> accountType) {
            this.alreadyLoggedIn = alreadyLoggedIn;
            this.succeed = succeed;
            this.exception = exception;
            this.basicUsername = basicUsername;
            this.basicPassword = basicPassword;
            this.oauth = oauth;
            this.user = user;
            this.authType = authType;
            this.color = color;
            this.apiUrlFormat = apiUrlFormat;
            this.sameOAuthSigningUrl = sameOAuthSigningUrl;
            this.noVersionSuffix = noVersionSuffix;
            this.accountType = accountType;
        }

        public SignInResponse(final boolean alreadyLoggedIn, final OAuthAuthorization oauth,
                              final User user, final int authType, final int color,
                              final String apiUrlFormat, final boolean sameOAuthSigningUrl,
                              final boolean noVersionSuffix, final Pair<String, String> accountType) {
            this(alreadyLoggedIn, true, null, null, null, oauth, user, authType, color, apiUrlFormat,
                    sameOAuthSigningUrl, noVersionSuffix, accountType);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final String basicUsername,
                              final String basicPassword, final User user, final int color,
                              final String apiUrlFormat, final boolean noVersionSuffix,
                              final Pair<String, String> accountType) {
            this(alreadyLoggedIn, true, null, basicUsername, basicPassword, null, user,
                    ParcelableCredentials.AUTH_TYPE_BASIC, color, apiUrlFormat, false,
                    noVersionSuffix, accountType);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final User user, final int color,
                              final String apiUrlFormat, final boolean noVersionSuffix,
                              final Pair<String, String> accountType) {
            this(alreadyLoggedIn, true, null, null, null, null, user,
                    ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE, color, apiUrlFormat, false,
                    noVersionSuffix, accountType);
        }

        private ContentValues toContentValues() {
            if (user == null) return null;
            final ContentValues values;
            switch (authType) {
                case ParcelableCredentials.AUTH_TYPE_BASIC: {
                    values = new ContentValues();
                    values.put(Accounts.BASIC_AUTH_USERNAME, basicUsername);
                    values.put(Accounts.BASIC_AUTH_PASSWORD, basicPassword);
                    values.put(Accounts.AUTH_TYPE, ParcelableCredentials.AUTH_TYPE_BASIC);
                    break;
                }
                case ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE: {
                    values = new ContentValues();
                    values.put(Accounts.AUTH_TYPE, ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE);
                    break;
                }
                case ParcelableCredentials.AUTH_TYPE_OAUTH:
                case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                    values = new ContentValues();
                    final OAuthToken accessToken = oauth.getOauthToken();
                    values.put(Accounts.OAUTH_TOKEN, accessToken.getOauthToken());
                    values.put(Accounts.OAUTH_TOKEN_SECRET, accessToken.getOauthTokenSecret());
                    values.put(Accounts.CONSUMER_KEY, oauth.getConsumerKey());
                    values.put(Accounts.CONSUMER_SECRET, oauth.getConsumerSecret());
                    values.put(Accounts.AUTH_TYPE, authType);
                    break;
                }
                default: {
                    return null;
                }
            }

            values.put(Accounts.ACCOUNT_KEY, UserKeyUtils.fromUser(user).toString());
            values.put(Accounts.SCREEN_NAME, user.getScreenName());
            values.put(Accounts.NAME, user.getName());
            values.put(Accounts.PROFILE_IMAGE_URL, TwitterContentUtils.getProfileImageUrl(user));
            values.put(Accounts.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());

            values.put(Accounts.COLOR, color);
            values.put(Accounts.IS_ACTIVATED, 1);


            values.put(Accounts.API_URL_FORMAT, apiUrlFormat);
            values.put(Accounts.SAME_OAUTH_SIGNING_URL, sameOAuthSigningUrl);
            values.put(Accounts.NO_VERSION_SUFFIX, noVersionSuffix);

            if (accountType != null) {
                values.put(Accounts.ACCOUNT_TYPE, accountType.first);
                values.put(Accounts.ACCOUNT_EXTRAS, accountType.second);
                final UserKey accountKey = UserKeyUtils.fromUser(user);
                final ParcelableUser parcelableUser = ParcelableUserUtils.fromUser(user, accountKey);
                values.put(Accounts.ACCOUNT_USER, JsonSerializer.serialize(parcelableUser, ParcelableUser.class));
            }
            return values;
        }
    }

    public static class SetConsumerKeySecretDialogFragment extends BaseSupportDialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(R.layout.dialog_set_consumer_key_secret);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final EditText editConsumerKey = (EditText) ((Dialog) dialog).findViewById(R.id.consumer_key);
                    final EditText editConsumerSecret = (EditText) ((Dialog) dialog).findViewById(R.id.consumer_secret);
                    final SharedPreferences prefs = SharedPreferencesWrapper.getInstance(getActivity(), SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    final SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(KEY_CONSUMER_KEY, ParseUtils.parseString(editConsumerKey.getText()));
                    editor.putString(KEY_CONSUMER_SECRET, ParseUtils.parseString(editConsumerSecret.getText()));
                    editor.apply();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final FragmentActivity activity = getActivity();
                    if (activity == null) return;
                    final MaterialEditText editConsumerKey = (MaterialEditText) ((Dialog) dialog).findViewById(R.id.consumer_key);
                    final MaterialEditText editConsumerSecret = (MaterialEditText) ((Dialog) dialog).findViewById(R.id.consumer_secret);
                    editConsumerKey.addValidator(new ConsumerKeySecretValidator(getString(R.string.invalid_consumer_key)));
                    editConsumerSecret.addValidator(new ConsumerKeySecretValidator(getString(R.string.invalid_consumer_secret)));
                    final SharedPreferences prefs = SharedPreferencesWrapper.getInstance(activity, SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    editConsumerKey.setText(prefs.getString(KEY_CONSUMER_KEY, null));
                    editConsumerSecret.setText(prefs.getString(KEY_CONSUMER_SECRET, null));
                }
            });
            return dialog;
        }
    }
}
