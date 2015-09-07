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

package org.mariotaku.twidere.activity.support;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.meizu.flyme.reflect.StatusBarProxy;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.TwitterOAuth;
import org.mariotaku.twidere.api.twitter.auth.BasicAuthorization;
import org.mariotaku.twidere.api.twitter.auth.EmptyAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthAuthorization;
import org.mariotaku.twidere.api.twitter.auth.OAuthToken;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.fragment.support.SupportProgressDialogFragment;
import org.mariotaku.twidere.graphic.EmptyDrawable;
import org.mariotaku.twidere.model.ParcelableCredentials;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.ContentValuesCreator;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.AuthenticationException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.AuthenticityTokenException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.WrongUserPassException;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereActionModeForChildListener;
import org.mariotaku.twidere.util.TwidereColorUtils;
import org.mariotaku.twidere.util.TwitterAPIFactory;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.support.ViewSupport;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;
import org.mariotaku.twidere.util.view.ConsumerKeySecretValidator;
import org.mariotaku.twidere.view.TintedStatusNativeActionModeAwareLayout;
import org.mariotaku.twidere.view.iface.TintedStatusLayout;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ContentValuesCreator.createAccount;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNonEmptyString;
import static org.mariotaku.twidere.util.Utils.isUserLoggedIn;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.trim;

public class SignInActivity extends BaseAppCompatActivity implements OnClickListener, TextWatcher {

    public static final String FRAGMENT_TAG_SIGN_IN_PROGRESS = "sign_in_progress";
    private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";
    private static final String EXTRA_API_LAST_CHANGE = "api_last_change";
    private static final String DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/";
    private final Handler mHandler = new Handler();
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
    private SharedPreferences mPreferences;
    private ContentResolver mResolver;
    private AbstractSignInTask mTask;
    private TintedStatusLayout mMainContent;

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
                if (resultCode == BaseAppCompatActivity.RESULT_OK && data != null) {
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
        mMainContent = (TintedStatusLayout) findViewById(R.id.main_content);
        setupTintStatusBar();
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
                final long[] account_ids = getActivatedAccountIds(this);
                if (account_ids.length > 0) {
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
        final boolean result = super.onPrepareOptionsMenu(menu);
        if (!shouldSetActionItemColor()) return result;
        final Toolbar toolbar = peekActionBarToolbar();
        if (toolbar != null) {
            final int themeColor = getCurrentThemeColor();
            final int themeId = getCurrentThemeResourceId();
            final int itemColor = ThemeUtils.getContrastForegroundColor(this, themeId, themeColor);
            ThemeUtils.wrapToolbarMenuIcon(ViewSupport.findViewByType(toolbar, ActionMenuView.class), itemColor, itemColor);
        }
        return result;
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
        setupWindow();
        super.onCreate(savedInstanceState);
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mResolver = getContentResolver();
        setContentView(R.layout.activity_sign_in);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));

        TwidereActionModeForChildListener actionModeForChildListener = new TwidereActionModeForChildListener(this, this, false);
        final TintedStatusNativeActionModeAwareLayout layout = (TintedStatusNativeActionModeAwareLayout) findViewById(R.id.main_content);
        layout.setActionModeForChildListener(actionModeForChildListener);

        ThemeUtils.setCompatContentViewOverlay(this, new EmptyDrawable());
        final View actionBarContainer = findViewById(R.id.twidere_action_bar_container);
        ViewCompat.setElevation(actionBarContainer, ThemeUtils.getSupportActionBarElevation(this));
        ViewSupport.setOutlineProvider(actionBarContainer, ViewOutlineProviderCompat.BACKGROUND);
        final View windowOverlay = findViewById(R.id.window_overlay);
        ViewSupport.setBackground(windowOverlay, ThemeUtils.getNormalWindowContentOverlay(this, getCurrentThemeResourceId()));

        if (savedInstanceState != null) {
            mAPIUrlFormat = savedInstanceState.getString(Accounts.API_URL_FORMAT);
            mAuthType = savedInstanceState.getInt(Accounts.AUTH_TYPE);
            mSameOAuthSigningUrl = savedInstanceState.getBoolean(Accounts.SAME_OAUTH_SIGNING_URL);
            mConsumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY));
            mConsumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET));
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

        final Resources resources = getResources();
        final ColorStateList color = ColorStateList.valueOf(resources.getColor(R.color.material_light_green));
        ViewCompat.setBackgroundTintList(mSignInButton, color);
        setSignInButton();

        final String consumerKey = mPreferences.getString(KEY_CONSUMER_KEY, null);
        final String consumerSecret = mPreferences.getString(KEY_CONSUMER_SECRET, null);
        if (savedInstanceState == null && !mPreferences.getBoolean(KEY_CONSUMER_KEY_SECRET_SET, false)
                && !Utils.isCustomConsumerKeySecret(consumerKey, consumerSecret)) {
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
        mUsername = ParseUtils.parseString(mEditUsername.getText());
        mPassword = ParseUtils.parseString(mEditPassword.getText());
    }

    private void setDefaultAPI() {
        final long apiLastChange = mPreferences.getLong(KEY_API_LAST_CHANGE, mAPIChangeTimestamp);
        final boolean defaultApiChanged = apiLastChange != mAPIChangeTimestamp;
        final String apiUrlFormat = getNonEmptyString(mPreferences, KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT);
        final int authType = mPreferences.getInt(KEY_AUTH_TYPE, ParcelableCredentials.AUTH_TYPE_OAUTH);
        final boolean sameOAuthSigningUrl = mPreferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false);
        final boolean noVersionSuffix = mPreferences.getBoolean(KEY_NO_VERSION_SUFFIX, false);
        final String consumerKey = getNonEmptyString(mPreferences, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
        final String consumerSecret = getNonEmptyString(mPreferences, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
        if (isEmpty(mAPIUrlFormat) || defaultApiChanged) {
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
        if (isEmpty(mConsumerKey) || defaultApiChanged) {
            mConsumerKey = consumerKey;
        }
        if (isEmpty(mConsumerSecret) || defaultApiChanged) {
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
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment f = fm.findFragmentByTag(FRAGMENT_TAG_SIGN_IN_PROGRESS);
        if (f instanceof DialogFragment) {
            ((DialogFragment) f).dismiss();
        }
        if (result != null) {
            if (result.alreadyLoggedIn) {
                final ContentValues values = result.toContentValues();
                if (values != null) {
                    mResolver.update(Accounts.CONTENT_URI, values, Expression.equals(Accounts.ACCOUNT_ID,
                            result.user.getId()).getSQL(), null);
                }
                Toast.makeText(this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show();
            } else if (result.succeed) {
                final ContentValues values = result.toContentValues();
                if (values != null) {
                    mResolver.insert(Accounts.CONTENT_URI, values);
                }
                final long loggedId = result.user.getId();
                final Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra(EXTRA_REFRESH_IDS, new long[]{loggedId});
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            } else {
                if (result.exception instanceof AuthenticityTokenException) {
                    Toast.makeText(this, R.string.wrong_api_key, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof WrongUserPassException) {
                    Toast.makeText(this, R.string.wrong_username_password, Toast.LENGTH_SHORT).show();
                } else if (result.exception instanceof AuthenticationException) {
                    showErrorMessage(this, getString(R.string.action_signing_in), result.exception.getCause(), true);
                } else {
                    showErrorMessage(this, getString(R.string.action_signing_in), result.exception, true);
                }
            }
        }
        setSignInButton();
    }

    void onSignInStart() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) return;
                final FragmentManager fm = getSupportFragmentManager();
                final FragmentTransaction ft = fm.beginTransaction();
                final SupportProgressDialogFragment fragment = new SupportProgressDialogFragment();
                fragment.setCancelable(false);
                fragment.show(ft, FRAGMENT_TAG_SIGN_IN_PROGRESS);
            }
        });
    }

    protected boolean isActionBarOutlineEnabled() {
        return true;
    }

    protected boolean shouldSetActionItemColor() {
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupActionBar();
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        final int themeColor = getCurrentThemeColor();
        final int themeId = getCurrentThemeResourceId();
        final String option = getThemeBackgroundOption();
        ThemeUtils.applyActionBarBackground(actionBar, this, themeId, themeColor, option, isActionBarOutlineEnabled());
    }

    private void setupTintStatusBar() {
        if (mMainContent == null) return;

        final int alpha = ThemeUtils.isTransparentBackground(getThemeBackgroundOption()) ? getCurrentThemeBackgroundAlpha() : 0xFF;
        final int statusBarColor = ThemeUtils.getActionBarColor(this, getCurrentThemeColor(), getCurrentThemeResourceId(), getThemeBackgroundOption());
        mMainContent.setColor(statusBarColor, alpha);
        StatusBarProxy.setStatusBarDarkIcon(getWindow(), TwidereColorUtils.getYIQLuminance(statusBarColor) > ThemeUtils.ACCENT_COLOR_THRESHOLD);

        mMainContent.setDrawShadow(false);
        mMainContent.setDrawColor(true);
        mMainContent.setFactor(1);
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    public static abstract class AbstractSignInTask extends AsyncTask<Object, Object, SignInResponse> {

        protected final SignInActivity callback;

        public AbstractSignInTask(final SignInActivity callback) {
            this.callback = callback;
        }

        @Override
        protected void onPostExecute(final SignInResponse result) {
            if (callback != null) {
                callback.onSignInResult(result);
            }
        }

        @Override
        protected void onPreExecute() {
            if (callback != null) {
                callback.onSignInStart();
            }
        }

        int analyseUserProfileColor(final User user) throws TwitterException {
            if (user == null) throw new TwitterException("Unable to get user info");
            return ParseUtils.parseColor("#" + user.getProfileLinkColor(), Color.TRANSPARENT);
        }

    }

    public static class BrowserSignInTask extends AbstractSignInTask {

        private final String oauthVerifier;

        private final Context context;
        @NonNull
        private final String apiUrlFormat;
        private final boolean sameOauthSigningUrl, noVersionSuffix;
        private final OAuthToken consumerKey, requestToken;

        public BrowserSignInTask(final SignInActivity context, OAuthToken consumerKey,
                                 final OAuthToken requestToken,
                                 final String oauthVerifier, @NonNull final String apiUrlFormat,
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
                Endpoint endpoint = TwitterAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", null,
                        sameOauthSigningUrl);
                final TwitterOAuth oauth = TwitterAPIFactory.getInstance(context, endpoint,
                        new OAuthAuthorization(consumerKey.getOauthToken(),
                                consumerKey.getOauthTokenSecret()), TwitterOAuth.class);
                final OAuthToken accessToken = oauth.getAccessToken(requestToken, oauthVerifier);
                final long userId = accessToken.getUserId();
                if (userId <= 0) return new SignInResponse(false, false, null);
                final OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(),
                        consumerKey.getOauthTokenSecret(), accessToken);
                endpoint = TwitterAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", versionSuffix,
                        sameOauthSigningUrl);
                final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint,
                        auth, Twitter.class);
                final User user = twitter.verifyCredentials();
                final int color = analyseUserProfileColor(user);
                return new SignInResponse(isUserLoggedIn(context, userId), auth, user,
                        ParcelableCredentials.AUTH_TYPE_OAUTH, color, apiUrlFormat, sameOauthSigningUrl,
                        noVersionSuffix);
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

        private final Context context;
        @NonNull
        private final String apiUrlFormat;
        private final boolean sameOAuthSigningUrl, noVersionSuffix;
        private final OAuthToken consumerKey;

        public SignInTask(final SignInActivity context, final String username, final String password, final int authType,
                          final OAuthToken consumerKey, @NonNull final String apiUrlFormat, final boolean sameOAuthSigningUrl,
                          final boolean noVersionSuffix) {
            super(context);
            this.context = context;
            this.username = username;
            this.password = password;
            this.authType = authType;
            this.consumerKey = consumerKey;
            this.apiUrlFormat = apiUrlFormat;
            this.sameOAuthSigningUrl = sameOAuthSigningUrl;
            this.noVersionSuffix = noVersionSuffix;
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
                e.printStackTrace();
                return new SignInResponse(false, false, e);
            } catch (final AuthenticationException e) {
                e.printStackTrace();
                return new SignInResponse(false, false, e);
            }
        }

        private SignInResponse authBasic() throws TwitterException {
            final String versionSuffix = noVersionSuffix ? null : "1.1";
            final Endpoint endpoint = new Endpoint(TwitterAPIFactory.getApiUrl(apiUrlFormat, "api", versionSuffix));
            final Authorization auth = new BasicAuthorization(username, password);
            final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint, auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final long userId = user.getId();
            if (userId <= 0) return new SignInResponse(false, false, null);
            if (isUserLoggedIn(context, userId)) return new SignInResponse(true, false, null);
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(isUserLoggedIn(context, userId), username, password, user,
                    color, apiUrlFormat, noVersionSuffix);
        }

        private SignInResponse authOAuth() throws AuthenticationException, TwitterException {
            Endpoint endpoint = TwitterAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", null, sameOAuthSigningUrl);
            OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret());
            final TwitterOAuth oauth = TwitterAPIFactory.getInstance(context, endpoint, auth, TwitterOAuth.class);
            final OAuthPasswordAuthenticator authenticator = new OAuthPasswordAuthenticator(oauth);
            final OAuthToken accessToken = authenticator.getOAuthAccessToken(username, password);
            final long userId = accessToken.getUserId();
            if (userId <= 0) return new SignInResponse(false, false, null);
            endpoint = TwitterAPIFactory.getOAuthRestEndpoint(apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix);
            auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret(), accessToken);
            final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint,
                    auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(isUserLoggedIn(context, userId), auth, user, ParcelableCredentials.AUTH_TYPE_OAUTH, color,
                    apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix);
        }

        private SignInResponse authTwipOMode() throws TwitterException {
            final String versionSuffix = noVersionSuffix ? null : "1.1";
            final Endpoint endpoint = new Endpoint(TwitterAPIFactory.getApiUrl(apiUrlFormat, "api", versionSuffix));
            final Authorization auth = new EmptyAuthorization();
            final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint, auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final long userId = user.getId();
            if (userId <= 0) return new SignInResponse(false, false, null);
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(isUserLoggedIn(context, userId), user, color, apiUrlFormat, noVersionSuffix);
        }

        private SignInResponse authxAuth() throws TwitterException {
            Endpoint endpoint = TwitterAPIFactory.getOAuthEndpoint(apiUrlFormat, "api", null, sameOAuthSigningUrl);
            OAuthAuthorization auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret());
            final TwitterOAuth oauth = TwitterAPIFactory.getInstance(context, endpoint, auth, TwitterOAuth.class);
            final OAuthToken accessToken = oauth.getAccessToken(username, password, TwitterOAuth.XAuthMode.CLIENT);
            final long userId = accessToken.getUserId();
            if (userId <= 0) return new SignInResponse(false, false, null);
            auth = new OAuthAuthorization(consumerKey.getOauthToken(), consumerKey.getOauthTokenSecret(), accessToken);
            endpoint = TwitterAPIFactory.getOAuthRestEndpoint(apiUrlFormat, sameOAuthSigningUrl, noVersionSuffix);
            final Twitter twitter = TwitterAPIFactory.getInstance(context, endpoint, auth, Twitter.class);
            final User user = twitter.verifyCredentials();
            final int color = analyseUserProfileColor(user);
            return new SignInResponse(isUserLoggedIn(context, userId), auth, user, ParcelableCredentials.AUTH_TYPE_XAUTH, color, apiUrlFormat,
                    sameOAuthSigningUrl, noVersionSuffix);
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
        public final boolean sameOauthSigningUrl, noVersionSuffix;

        public SignInResponse(final boolean alreadyLoggedIn, final boolean succeed, final Exception exception) {
            this(alreadyLoggedIn, succeed, exception, null, null, null, null, 0, 0, null, false, false);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final boolean succeed, final Exception exception,
                              final String basicUsername, final String basicPassword,
                              final OAuthAuthorization oauth, final User user, final int authType, final int color,
                              final String apiUrlFormat, final boolean sameOauthSigningUrl, final boolean noVersionSuffix) {
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
            this.sameOauthSigningUrl = sameOauthSigningUrl;
            this.noVersionSuffix = noVersionSuffix;
        }

        public SignInResponse(final boolean alreadyLoggedIn, final OAuthAuthorization oauth,
                              final User user, final int authType, final int color,
                              final String apiUrlFormat, final boolean sameOauthSigningUrl,
                              final boolean noVersionSuffix) {
            this(alreadyLoggedIn, true, null, null, null, oauth, user, authType, color, apiUrlFormat,
                    sameOauthSigningUrl, noVersionSuffix);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final String basicUsername,
                              final String basicPassword, final User user, final int color,
                              final String apiUrlFormat, final boolean noVersionSuffix) {
            this(alreadyLoggedIn, true, null, basicUsername, basicPassword, null, user, ParcelableCredentials.AUTH_TYPE_BASIC, color,
                    apiUrlFormat, false, noVersionSuffix);
        }

        public SignInResponse(final boolean alreadyLoggedIn, final User user, final int color,
                              final String apiUrlFormat, final boolean noVersionSuffix) {
            this(alreadyLoggedIn, true, null, null, null, null, user, ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE, color,
                    apiUrlFormat, false, noVersionSuffix);
        }

        private ContentValues toContentValues() {
            final ContentValues values;
            switch (authType) {
                case ParcelableCredentials.AUTH_TYPE_BASIC: {
                    values = createAccount(basicUsername, basicPassword, user, color, apiUrlFormat,
                            noVersionSuffix);
                    break;
                }
                case ParcelableCredentials.AUTH_TYPE_TWIP_O_MODE: {
                    values = ContentValuesCreator.createAccount(user, color, apiUrlFormat, noVersionSuffix);
                    break;
                }
                case ParcelableCredentials.AUTH_TYPE_OAUTH:
                case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                    values = ContentValuesCreator.createAccount(oauth, user, authType, color, apiUrlFormat,
                            sameOauthSigningUrl, noVersionSuffix);
                    break;
                }
                default: {
                    values = null;
                }
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
