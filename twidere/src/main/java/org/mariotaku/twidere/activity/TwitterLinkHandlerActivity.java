package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

public class TwitterLinkHandlerActivity extends Activity implements Constants {

	public static final String[] TWITTER_RESERVED_PATHS = { "about", "account", "accounts", "activity", "all",
			"announcements", "anywhere", "api_rules", "api_terms", "apirules", "apps", "auth", "badges", "blog",
			"business", "buttons", "contacts", "devices", "direct_messages", "download", "downloads",
			"edit_announcements", "faq", "favorites", "find_sources", "find_users", "followers", "following",
			"friend_request", "friendrequest", "friends", "goodies", "help", "home", "im_account", "inbox",
			"invitations", "invite", "jobs", "list", "login", "logo", "logout", "me", "mentions", "messages",
			"mockview", "newtwitter", "notifications", "nudge", "oauth", "phoenix_search", "positions", "privacy",
			"public_timeline", "related_tweets", "replies", "retweeted_of_mine", "retweets", "retweets_by_others",
			"rules", "saved_searches", "search", "sent", "settings", "share", "signup", "signin", "similar_to",
			"statistics", "terms", "tos", "translate", "trends", "tweetbutton", "twttr", "update_discoverability",
			"users", "welcome", "who_to_follow", "widgets", "zendesk_auth", "media_signup" };

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private static final String AUTHORITY_TWITTER_COM = "twitter.com";

	private static final int URI_CODE_TWITTER_STATUS = 1;
	private static final int URI_CODE_TWITTER_USER = 2;
	private static final int URI_CODE_TWITTER_USER_FOLLOWING = 11;
	private static final int URI_CODE_TWITTER_USER_FOLLOWERS = 12;
	private static final int URI_CODE_TWITTER_USER_FAVORITES = 13;
	private static final int URI_CODE_TWITTER_INTENT_TWEET = 101;
	private static final int URI_CODE_TWITTER_REDIRECT = 201;

	static {
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/i/redirect", URI_CODE_TWITTER_REDIRECT);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/intent/tweet", URI_CODE_TWITTER_INTENT_TWEET);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#", URI_CODE_TWITTER_STATUS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#/photo/#", URI_CODE_TWITTER_STATUS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*", URI_CODE_TWITTER_USER);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/following", URI_CODE_TWITTER_USER_FOLLOWING);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/followers", URI_CODE_TWITTER_USER_FOLLOWERS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/favorites", URI_CODE_TWITTER_USER_FAVORITES);
	}

	private SharedPreferences mPreferences;
	private PackageManager mPackageManager;

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_PICK_ACTIVITY: {
				if (resultCode != RESULT_OK || data == null || !data.hasExtra(EXTRA_DATA)
						|| !data.hasExtra(EXTRA_INTENT)) {
					finish();
					return;
				}
				final ResolveInfo resolveInfo = data.getParcelableExtra(EXTRA_DATA);
				final Intent extraIntent = data.getParcelableExtra(EXTRA_INTENT);
				final ActivityInfo activityInfo = resolveInfo.activityInfo;
				if (activityInfo == null) {
					finish();
					return;
				}
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putString(KEY_FALLBACK_TWITTER_LINK_HANDLER, activityInfo.packageName);
				editor.apply();
				final Intent intent = new Intent(Intent.ACTION_VIEW, extraIntent.getData());
				intent.setClassName(activityInfo.packageName, activityInfo.name);
				startActivity(intent);
				finish();
				return;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPackageManager = getPackageManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null) {
			finish();
			return;
		}
		final Uri uri = data.buildUpon().authority(AUTHORITY_TWITTER_COM).build();
		final Intent handledIntent = getHandledIntent(uri);
		if (handledIntent != null) {
			startActivity(handledIntent);
		} else {
			final String packageName = mPreferences.getString(KEY_FALLBACK_TWITTER_LINK_HANDLER, null);
			final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
			fallbackIntent.setPackage(packageName);
			if (TextUtils.isEmpty(packageName) || mPackageManager.queryIntentActivities(fallbackIntent, 0).isEmpty()) {
				final Intent pickIntent = new Intent(INTENT_ACTION_PICK_ACTIVITY);
				pickIntent.putExtra(EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, uri));
				pickIntent.putExtra(EXTRA_BLACKLIST, new String[] { getPackageName() });
				startActivityForResult(pickIntent, REQUEST_PICK_ACTIVITY);
				return;
			} else {
				startActivity(fallbackIntent);
			}
		}
		finish();
	}

	private Intent getHandledIntent(final Uri uri) {
		final List<String> pathSegments = uri.getPathSegments();
		switch (URI_MATCHER.match(uri)) {
			case URI_CODE_TWITTER_STATUS: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_STATUS);
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments.get(2));
				return new Intent(Intent.ACTION_VIEW, builder.build());
			}
			case URI_CODE_TWITTER_INTENT_TWEET: {
				final Intent handledIntent = new Intent(this, ComposeActivity.class);
				handledIntent.setAction(Intent.ACTION_SEND);
				final String text = uri.getQueryParameter("text");
				final String url = uri.getQueryParameter("url");
				handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
				return handledIntent;
			}
			case URI_CODE_TWITTER_USER: {
				final String pathSegment = pathSegments.get(0);
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				if ("share".equals(pathSegment)) {
					final Intent handledIntent = new Intent(this, ComposeActivity.class);
					handledIntent.setAction(Intent.ACTION_SEND);
					final String text = uri.getQueryParameter("text");
					final String url = uri.getQueryParameter("url");
					handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
					return handledIntent;
				} else if ("following".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FRIENDS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("followers".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FOLLOWERS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("favorites".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FAVORITES);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if (!ArrayUtils.contains(TWITTER_RESERVED_PATHS, pathSegment)) {
					builder.authority(AUTHORITY_USER);
					builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegment);
				} else
					return null;
				return new Intent(Intent.ACTION_VIEW, builder.build());
			}
			case URI_CODE_TWITTER_USER_FOLLOWING: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FRIENDS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				return new Intent(Intent.ACTION_VIEW, builder.build());
			}
			case URI_CODE_TWITTER_USER_FOLLOWERS: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FOLLOWERS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				return new Intent(Intent.ACTION_VIEW, builder.build());
			}
			case URI_CODE_TWITTER_USER_FAVORITES: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FAVORITES);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				return new Intent(Intent.ACTION_VIEW, builder.build());
			}
		}
		return null;
	}

}
