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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getNonEmptyString;
import static org.mariotaku.twidere.util.Utils.setUserAgent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.net.TwidereHostResolverFactory;
import org.mariotaku.twidere.util.net.TwidereHttpClientFactory;
import org.xmlpull.v1.XmlPullParserException;

import twitter4j.Twitter;
import twitter4j.TwitterConstants;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.StringReader;

@SuppressLint("SetJavaScriptEnabled")
public class BrowserSignInActivity extends BaseSupportDialogActivity implements TwitterConstants {

	private static final String INJECT_CONTENT = "javascript:window.injector.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');";

	private SharedPreferences mPreferences;

	private WebView mWebView;
	private View mProgressContainer;

	private WebSettings mWebSettings;

	private RequestToken mRequestToken;

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
			case MENU_HOME: {
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setContentView(R.layout.activity_browser_sign_in);
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
		if (mRequestToken != null || mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mTask = new GetRequestTokenTask(this);
		mTask.execute();
	}

	private void loadUrl(final String url) {
		if (mWebView == null) return;
		mWebView.loadUrl(url);
	}

	private String readOAuthPin(final String html) {
		try {
			return OAuthPasswordAuthenticator.readOAuthPINFromHtml(new StringReader(html));
		} catch (final XmlPullParserException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setLoadProgressShown(final boolean shown) {
		mProgressContainer.setVisibility(shown ? View.VISIBLE : View.GONE);
	}

	private void setRequestToken(final RequestToken token) {
		mRequestToken = token;
	}

	static class AuthorizationWebViewClient extends WebViewClient {
		private final BrowserSignInActivity mActivity;

		AuthorizationWebViewClient(final BrowserSignInActivity activity) {
			mActivity = activity;
		}

		@Override
		public void onPageFinished(final WebView view, final String url) {
			super.onPageFinished(view, url);
			view.loadUrl(INJECT_CONTENT);
			mActivity.setLoadProgressShown(false);
		}

		@Override
		public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mActivity.setLoadProgressShown(true);
		}

		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description,
				final String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			mActivity.finish();
		}

		@Override
		public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
			if (mActivity.mPreferences.getBoolean(KEY_IGNORE_SSL_ERROR, false)) {
				handler.proceed();
			} else {
				handler.cancel();
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			final Uri uri = Uri.parse(url);
			if (url.startsWith(OAUTH_CALLBACK_URL)) {
				final String oauth_verifier = uri.getQueryParameter(EXTRA_OAUTH_VERIFIER);
				final RequestToken request_token = mActivity.mRequestToken;
				if (oauth_verifier != null && request_token != null) {
					final Intent intent = new Intent();
					intent.putExtra(EXTRA_OAUTH_VERIFIER, oauth_verifier);
					intent.putExtra(EXTRA_REQUEST_TOKEN, request_token.getToken());
					intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, request_token.getTokenSecret());
					mActivity.setResult(RESULT_OK, intent);
					mActivity.finish();
				}
				return true;
			}
			return false;
		}

	}

	static class GetRequestTokenTask extends AsyncTask<Void, Void, RequestToken> {

		private final String mConsumerKey, mConsumerSecret;
		private final TwidereApplication mApplication;
		private final SharedPreferences mPreferences;
		private final BrowserSignInActivity mActivity;

		public GetRequestTokenTask(final BrowserSignInActivity activity) {
			mActivity = activity;
			mApplication = TwidereApplication.getInstance(activity);
			mPreferences = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			final Intent intent = activity.getIntent();
			mConsumerKey = intent.getStringExtra(Accounts.CONSUMER_KEY);
			mConsumerSecret = intent.getStringExtra(Accounts.CONSUMER_SECRET);
		}

		@Override
		protected RequestToken doInBackground(final Void... params) {
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			final boolean enable_gzip_compressing = mPreferences.getBoolean(KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = mPreferences.getBoolean(KEY_IGNORE_SSL_ERROR, false);
			final boolean enable_proxy = mPreferences.getBoolean(KEY_ENABLE_PROXY, false);
			final String consumer_key = getNonEmptyString(mPreferences, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
			final String consumer_secret = getNonEmptyString(mPreferences, KEY_CONSUMER_SECRET,
					TWITTER_CONSUMER_SECRET_2);
			cb.setHostAddressResolverFactory(new TwidereHostResolverFactory(mApplication));
			cb.setHttpClientFactory(new TwidereHttpClientFactory(mApplication));
			setUserAgent(mActivity, cb);
			cb.setRestBaseURL(DEFAULT_REST_BASE_URL);
			cb.setOAuthBaseURL(DEFAULT_OAUTH_BASE_URL);
			cb.setSigningRestBaseURL(DEFAULT_SIGNING_REST_BASE_URL);
			cb.setSigningOAuthBaseURL(DEFAULT_SIGNING_OAUTH_BASE_URL);
			if (!isEmpty(mConsumerKey) && !isEmpty(mConsumerSecret)) {
				cb.setOAuthConsumerKey(mConsumerKey);
				cb.setOAuthConsumerSecret(mConsumerSecret);
			} else {
				cb.setOAuthConsumerKey(consumer_key);
				cb.setOAuthConsumerSecret(consumer_secret);
			}
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			if (enable_proxy) {
				final String proxy_host = mPreferences.getString(KEY_PROXY_HOST, null);
				final int proxy_port = ParseUtils.parseInt(mPreferences.getString(KEY_PROXY_PORT, "-1"));
				if (!isEmpty(proxy_host) && proxy_port > 0) {
					cb.setHttpProxyHost(proxy_host);
					cb.setHttpProxyPort(proxy_port);
				}
			}
			try {
				final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
				return twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final RequestToken data) {
			mActivity.setLoadProgressShown(false);
			mActivity.setRequestToken(data);
			if (data == null) {
				Toast.makeText(mActivity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
				mActivity.finish();
				return;
			}
			mActivity.loadUrl(data.getAuthorizationURL());
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
			final String oauth_verifier = mActivity.readOAuthPin(html);
			final RequestToken request_token = mActivity.mRequestToken;
			if (oauth_verifier != null && request_token != null) {
				final Intent intent = new Intent();
				intent.putExtra(EXTRA_OAUTH_VERIFIER, oauth_verifier);
				intent.putExtra(EXTRA_REQUEST_TOKEN, request_token.getToken());
				intent.putExtra(EXTRA_REQUEST_TOKEN_SECRET, request_token.getTokenSecret());
				mActivity.setResult(RESULT_OK, intent);
				mActivity.finish();
			}
		}
	}
}
