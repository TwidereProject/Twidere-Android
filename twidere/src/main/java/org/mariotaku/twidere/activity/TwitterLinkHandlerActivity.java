package org.mariotaku.twidere.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.ComposeActivity;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;

public class TwitterLinkHandlerActivity extends Activity implements Constants {

    @SuppressWarnings("SpellCheckingInspection")
    public static final String[] TWITTER_RESERVED_PATHS = {"about", "account", "accounts", "activity", "all",
            "announcements", "anywhere", "api_rules", "api_terms", "apirules", "apps", "auth", "badges", "blog",
            "business", "buttons", "contacts", "devices", "direct_messages", "download", "downloads",
            "edit_announcements", "faq", "favorites", "find_sources", "find_users", "followers", "following",
            "friend_request", "friendrequest", "friends", "goodies", "help", "home", "im_account", "inbox",
            "invitations", "invite", "jobs", "list", "login", "logo", "logout", "me", "mentions", "messages",
            "mockview", "newtwitter", "notifications", "nudge", "oauth", "phoenix_search", "positions", "privacy",
            "public_timeline", "related_tweets", "replies", "retweeted_of_mine", "retweets", "retweets_by_others",
            "rules", "saved_searches", "search", "sent", "settings", "share", "signup", "signin", "similar_to",
            "statistics", "terms", "tos", "translate", "trends", "tweetbutton", "twttr", "update_discoverability",
            "users", "welcome", "who_to_follow", "widgets", "zendesk_auth", "media_signup"};


    private static final String AUTHORITY_TWITTER_COM = "twitter.com";

    private SharedPreferences mPreferences;

    private static Uri regulateTwitterUri(Uri data) {
        final String encodedFragment = data.getEncodedFragment();
        if (encodedFragment != null && encodedFragment.startsWith("!/")) {
            return regulateTwitterUri(Uri.parse("https://twitter.com" + encodedFragment.substring(1)));
        }
        final Uri.Builder builder = data.buildUpon();
        builder.scheme("https");
        builder.authority(AUTHORITY_TWITTER_COM);
        return builder.build();
    }

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
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PackageManager packageManager = getPackageManager();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        final Intent intent = getIntent();
        final Uri data = intent.getData();
        if (data == null) {
            finish();
            return;
        }
        final Uri uri = regulateTwitterUri(data);
        final Pair<Intent, Boolean> handled = getHandledIntent(uri);
        if (handled.first != null) {
            startActivity(handled.first);
        } else {
            if (!handled.second) {
//                AbsLogger.error(new TwitterLinkException("Unable to handle twitter uri " + uri));
            }
            final String packageName = mPreferences.getString(KEY_FALLBACK_TWITTER_LINK_HANDLER, null);
            final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
            fallbackIntent.setPackage(packageName);
            if (TextUtils.isEmpty(packageName) || packageManager.queryIntentActivities(fallbackIntent, 0).isEmpty()) {
                final Intent pickIntent = new Intent(INTENT_ACTION_PICK_ACTIVITY);
                pickIntent.putExtra(EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, uri));
                pickIntent.putExtra(EXTRA_BLACKLIST, new String[]{getPackageName()});
                startActivityForResult(pickIntent, REQUEST_PICK_ACTIVITY);
                return;
            } else {
                startActivity(fallbackIntent);
            }
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    @NonNull
    private Pair<Intent, Boolean> getHandledIntent(final Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() > 0) {
            switch (pathSegments.get(0)) {
                case "i": {
                    return getIUriIntent(uri, pathSegments);
                }
                case "intent": {
                    return getIntentUriIntent(uri, pathSegments);
                }
                case "share": {
                    final Intent handledIntent = new Intent(this, ComposeActivity.class);
                    handledIntent.setAction(Intent.ACTION_SEND);
                    final String text = uri.getQueryParameter("text");
                    final String url = uri.getQueryParameter("url");
                    handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
                    return Pair.create(handledIntent, true);
                }
                case "search": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_SEARCH);
                    builder.appendQueryParameter(QUERY_PARAM_QUERY, uri.getQueryParameter("q"));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "following": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FRIENDS);
                    builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "followers": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FOLLOWERS);
                    builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "favorites": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FAVORITES);
                    builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                default: {
                    if (ArrayUtils.contains(TWITTER_RESERVED_PATHS, pathSegments.get(0))) {
                        return Pair.create(null, true);
                    }
                    return handleUserSpecificPageIntent(uri, pathSegments, pathSegments.get(0));
                }
            }
        }
        return Pair.create(null, false);
    }

    private Pair<Intent, Boolean> handleUserSpecificPageIntent(Uri uri, List<String> pathSegments, String screenName) {
        final int segsSize = pathSegments.size();
        if (segsSize == 1) {
            final Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEME_TWIDERE);
            builder.authority(AUTHORITY_USER);
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
            return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
        } else if (segsSize == 2) {
            switch (pathSegments.get(1)) {
                case "following": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FRIENDS);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "followers": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FOLLOWERS);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "favorites": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_FAVORITES);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                default: {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWIDERE);
                    builder.authority(AUTHORITY_USER_LIST);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
            }
        } else if (segsSize >= 3) {
            if ("status".equals(pathSegments.get(1)) && ParseUtils.parseLong(pathSegments.get(2), -1) != -1) {
                final Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME_TWIDERE);
                builder.authority(AUTHORITY_STATUS);
                builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments.get(2));
                return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
            } else {
                switch (pathSegments.get(2)) {
                    case "members": {
                        final Uri.Builder builder = new Uri.Builder();
                        builder.scheme(SCHEME_TWIDERE);
                        builder.authority(AUTHORITY_USER_LIST_MEMBERS);
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                        return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                    }
                    case "subscribers": {
                        final Uri.Builder builder = new Uri.Builder();
                        builder.scheme(SCHEME_TWIDERE);
                        builder.authority(AUTHORITY_USER_LIST_SUBSCRIBERS);
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                        return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                    }
                }
            }
        }
        return Pair.create(null, false);
    }

    private Pair<Intent, Boolean> getIntentUriIntent(Uri uri, List<String> pathSegments) {
        if (pathSegments.size() < 2) return Pair.create(null, false);
        switch (pathSegments.get(1)) {
            case "tweet": {
                final Intent handledIntent = new Intent(this, ComposeActivity.class);
                handledIntent.setAction(Intent.ACTION_SEND);
                final String text = uri.getQueryParameter("text");
                final String url = uri.getQueryParameter("url");
                handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
                return Pair.create(handledIntent, true);
            }
        }
        return Pair.create(null, false);
    }

    private Pair<Intent, Boolean> getIUriIntent(Uri uri, List<String> pathSegments) {
        return Pair.create(null, false);
    }

    private class TwitterLinkException extends Exception {
        public TwitterLinkException(final String s) {
            super(s);
        }
    }
}
