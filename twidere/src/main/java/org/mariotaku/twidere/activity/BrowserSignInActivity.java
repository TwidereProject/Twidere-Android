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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.attoparser.ParseException;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.TwitterOAuth;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.oauth.OAuthAuthorization;
import org.mariotaku.restfu.oauth.OAuthToken;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TwidereDataStore.Accounts;
import org.mariotaku.twidere.util.AsyncTaskUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;
import org.mariotaku.twidere.util.webkit.DefaultWebViewClient;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

@SuppressLint("SetJavaScriptEnabled")
public class BrowserSignInActivity extends BaseActivity {

    private static final String INJECT_CONTENT = "javascript:window.injector.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');";

    private WebView mWebView;
    private View mProgressContainer;

    private WebSettings mWebSettings;

    private OAuthToken mRequestToken;

    private GetRequestTokenTask mTask;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mWebView = (WebView) findViewById(R.id.webview);
        mProgressContainer = findViewById(R.id.progress_container);
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
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("AddJavascriptInterface")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser_sign_in);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //noinspection deprecation
            CookieManager.getInstance().removeAllCookie();
        } else {
            CookieManager.getInstance().removeAllCookies(null);
        }
        mWebView.setWebViewClient(new AuthorizationWebViewClient(this));
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.addJavascriptInterface(new InjectorJavaScriptInterface(this), "injector");
        mWebSettings = mWebView.getSettings();
        mWebSettings.setLoadsImagesAutomatically(true);
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setBlockNetworkImage(false);
        mWebSettings.setSaveFormData(true);
        getRequestToken();
    }

    private void getRequestToken() {
        if (mRequestToken != null || mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING)
            return;
        mTask = new GetRequestTokenTask(this);
        AsyncTaskUtils.executeTask(mTask);
    }

    private void loadUrl(final String url) {
        if (mWebView == null) return;
        mWebView.loadUrl(url);
    }

    private String readOAuthPin(final String html) {
        try {
            OAuthPasswordAuthenticator.OAuthPinData data = new OAuthPasswordAuthenticator.OAuthPinData();
            OAuthPasswordAuthenticator.Companion.readOAuthPINFromHtml(new StringReader(html), data);
            return data.getOauthPin();
        } catch (final ParseException | IOException e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }

    private void setLoadProgressShown(final boolean shown) {
        mProgressContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    private void setRequestToken(final OAuthToken token) {
        mRequestToken = token;
    }

    static class AuthorizationWebViewClient extends DefaultWebViewClient {

        AuthorizationWebViewClient(final BrowserSignInActivity activity) {
            super(activity);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);
            view.loadUrl(INJECT_CONTENT);
            final BrowserSignInActivity activity = (BrowserSignInActivity) getActivity();
            activity.setLoadProgressShown(false);
            Uri uri = Uri.parse(url);
            // Hack for fanfou
            if ("fanfou.com".equals(uri.getHost())) {
                final String path = uri.getPath();
                final Set<String> paramNames = uri.getQueryParameterNames();
                if ("/oauth/authorize".equals(path) && paramNames.contains("oauth_callback")) {
                    // Sign in successful response.
                    final OAuthToken requestToken = activity.mRequestToken;
                    if (requestToken != null) {
                        final Intent intent = new Intent();
                        intent.putExtra(EXTRA_REQUEST_TOKEN, requestToken.getOauthToken());
                        intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, requestToken.getOauthTokenSecret());
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                    }
                }
            }
        }

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            ((BrowserSignInActivity) getActivity()).setLoadProgressShown(true);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description,
                                    final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            final Activity activity = getActivity();
            Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            activity.finish();
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            final Uri uri = Uri.parse(url);
            if (url.startsWith(OAUTH_CALLBACK_URL)) {
                final String oauthVerifier = uri.getQueryParameter(EXTRA_OAUTH_VERIFIER);
                final BrowserSignInActivity activity = (BrowserSignInActivity) getActivity();
                final OAuthToken requestToken = activity.mRequestToken;
                if (oauthVerifier != null && requestToken != null) {
                    final Intent intent = new Intent();
                    intent.putExtra(EXTRA_OAUTH_VERIFIER, oauthVerifier);
                    intent.putExtra(EXTRA_REQUEST_TOKEN, requestToken.getOauthToken());
                    intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, requestToken.getOauthTokenSecret());
                    activity.setResult(RESULT_OK, intent);
                    activity.finish();
                }
                return true;
            }
            return false;
        }

    }

    static class GetRequestTokenTask extends AsyncTask<Object, Object, SingleResponse<OAuthToken>> {

        private final String mConsumerKey, mConsumerSecret;
        private final BrowserSignInActivity mActivity;
        private final String mAPIUrlFormat;
        private final boolean mSameOAuthSigningUrl;

        public GetRequestTokenTask(final BrowserSignInActivity activity) {
            mActivity = activity;
            final Intent intent = activity.getIntent();
            mConsumerKey = intent.getStringExtra(Accounts.CONSUMER_KEY);
            mConsumerSecret = intent.getStringExtra(Accounts.CONSUMER_SECRET);
            mAPIUrlFormat = intent.getStringExtra(Accounts.API_URL_FORMAT);
            mSameOAuthSigningUrl = intent.getBooleanExtra(Accounts.SAME_OAUTH_SIGNING_URL, true);
        }

        @Override
        protected SingleResponse<OAuthToken> doInBackground(final Object... params) {
            if (isEmpty(mConsumerKey) || isEmpty(mConsumerSecret)) {
                return new SingleResponse<>();
            }
            try {
                final Endpoint endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(mAPIUrlFormat,
                        mSameOAuthSigningUrl);
                final Authorization auth = new OAuthAuthorization(mConsumerKey, mConsumerSecret);
                final TwitterOAuth oauth = MicroBlogAPIFactory.getInstance(mActivity, endpoint,
                        auth, TwitterOAuth.class);
                return new SingleResponse<>(oauth.getRequestToken(OAUTH_CALLBACK_OOB), null, new Bundle());
            } catch (final MicroBlogException e) {
                return new SingleResponse<>(null, e, new Bundle());
            }
        }

        @Override
        protected void onPostExecute(final SingleResponse<OAuthToken> result) {
            mActivity.setLoadProgressShown(false);
            if (result.hasData()) {
                final OAuthToken token = result.getData();
                mActivity.setRequestToken(token);
                final Endpoint endpoint = MicroBlogAPIFactory.getOAuthSignInEndpoint(mAPIUrlFormat, true);
                mActivity.loadUrl(endpoint.construct("/oauth/authorize", new String[]{"oauth_token", token.getOauthToken()}));
            } else {
                if (BuildConfig.DEBUG && result.hasException()) {
                    Log.w(LOGTAG, "Exception while browser sign in", result.getException());
                }
                if (!mActivity.isFinishing()) {
                    Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    mActivity.finish();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mActivity.setLoadProgressShown(true);
        }

    }

    static class InjectorJavaScriptInterface {

        private final BrowserSignInActivity mActivity;

        InjectorJavaScriptInterface(final BrowserSignInActivity activity) {
            mActivity = activity;
        }

        @JavascriptInterface
        public void processHTML(final String html) {
            final String oauthVerifier = mActivity.readOAuthPin(html);
            final OAuthToken requestToken = mActivity.mRequestToken;
            if (oauthVerifier != null && requestToken != null) {
                final Intent intent = new Intent();
                intent.putExtra(EXTRA_OAUTH_VERIFIER, oauthVerifier);
                intent.putExtra(EXTRA_REQUEST_TOKEN, requestToken.getOauthToken());
                intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, requestToken.getOauthTokenSecret());
                mActivity.setResult(RESULT_OK, intent);
                mActivity.finish();
            }
        }
    }
}
