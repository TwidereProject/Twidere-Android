package org.mariotaku.twidere.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import org.apache.commons.lang3.ArrayUtils
import org.mariotaku.ktextension.toLong
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.Analyzer
import org.mariotaku.twidere.util.IntentUtils
import org.mariotaku.twidere.util.Utils

class WebLinkHandlerActivity : Activity(), Constants {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageManager = packageManager
        val intent = intent
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        val uri = intent.data
        if (uri == null || uri.host == null) {
            finish()
            return
        }
        val handled: Pair<Intent, Boolean>
        when (uri.host) {
            "twitter.com", "www.twitter.com", "mobile.twitter.com" -> {
                handled = handleTwitterLink(regulateTwitterUri(uri))
            }
            "fanfou.com" -> {
                handled = handleFanfouLink(uri)
            }
            else -> {
                handled = Pair.create<Intent, Boolean>(null, false)
            }
        }
        if (handled.first != null) {
            handled.first.putExtras(intent)
            startActivity(handled.first)
        } else {
            if (!handled.second) {
                Analyzer.logException(TwitterLinkException("Unable to handle twitter uri " + uri))
            }
            val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
            fallbackIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            fallbackIntent.`package` = IntentUtils.getDefaultBrowserPackage(this, uri, false)
            val componentName = fallbackIntent.resolveActivity(packageManager)
            if (componentName == null) {
                val targetIntent = Intent(Intent.ACTION_VIEW, uri)
                targetIntent.addCategory(Intent.CATEGORY_BROWSABLE)
                startActivity(Intent.createChooser(targetIntent, getString(R.string.open_in_browser)))
            } else if (!TextUtils.equals(packageName, componentName.packageName)) {
                startActivity(fallbackIntent)
            } else {
                // TODO show error
            }
        }
        finish()
    }

    override fun onStart() {
        super.onStart()
        setVisible(true)
    }

    private fun handleFanfouLink(uri: Uri): Pair<Intent, Boolean> {
        val pathSegments = uri.pathSegments
        if (pathSegments.size > 0) {
            when (pathSegments[0]) {
                "statuses" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_STATUS)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_FANFOU_COM)
                    builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments[1])
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                else -> {
                    if (!ArrayUtils.contains(FANFOU_RESERVED_PATHS, pathSegments[0])) {
                        if (pathSegments.size == 1) {
                            val builder = Uri.Builder()
                            builder.scheme(SCHEME_TWIDERE)
                            builder.authority(AUTHORITY_USER)
                            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_FANFOU_COM)
                            val userKey = UserKey(pathSegments[0], USER_TYPE_FANFOU_COM)
                            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
                            return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                        }
                    }
                    return Pair.create<Intent, Boolean>(null, false)
                }
            }
        }
        return Pair.create<Intent, Boolean>(null, false)
    }

    private fun handleTwitterLink(uri: Uri): Pair<Intent, Boolean> {
        val pathSegments = uri.pathSegments
        if (pathSegments.size > 0) {
            when (pathSegments[0]) {
                "i" -> {
                    return getIUriIntent(uri, pathSegments)
                }
                "intent" -> {
                    return getTwitterIntentUriIntent(uri, pathSegments)
                }
                "share" -> {
                    val handledIntent = Intent(this, ComposeActivity::class.java)
                    handledIntent.action = Intent.ACTION_SEND
                    val text = uri.getQueryParameter("text")
                    val url = uri.getQueryParameter("url")
                    handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url))
                    return Pair.create(handledIntent, true)
                }
                "search" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_SEARCH)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_QUERY, uri.getQueryParameter("q"))
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "hashtag" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_SEARCH)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_QUERY, "#${uri.lastPathSegment}")
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "following" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FRIENDS)
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString())
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "followers" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FOLLOWERS)
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString())
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "favorites" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FAVORITES)
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString())
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                else -> {
                    if (ArrayUtils.contains(TWITTER_RESERVED_PATHS, pathSegments[0])) {
                        return Pair.create<Intent, Boolean>(null, true)
                    }
                    return handleUserSpecificPageIntent(uri, pathSegments, pathSegments[0])
                }
            }
        }
        return Pair.create<Intent, Boolean>(null, false)
    }

    private fun handleUserSpecificPageIntent(uri: Uri, pathSegments: List<String>, screenName: String): Pair<Intent, Boolean> {
        val segsSize = pathSegments.size
        if (segsSize == 1) {
            val builder = Uri.Builder()
            builder.scheme(SCHEME_TWIDERE)
            builder.authority(AUTHORITY_USER)
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
            return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
        } else if (segsSize == 2) {
            when (pathSegments[1]) {
                "following" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FRIENDS)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "followers" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FOLLOWERS)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                "favorites" -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_FAVORITES)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
                else -> {
                    val builder = Uri.Builder()
                    builder.scheme(SCHEME_TWIDERE)
                    builder.authority(AUTHORITY_USER_LIST)
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                    builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments[1])
                    return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                }
            }
        } else if (segsSize >= 3) {
            if ("status" == pathSegments[1] && pathSegments[2].toLong(-1) != -1L) {
                val builder = Uri.Builder()
                builder.scheme(SCHEME_TWIDERE)
                builder.authority(AUTHORITY_STATUS)
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments[2])
                return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
            } else {
                when (pathSegments[2]) {
                    "members" -> {
                        val builder = Uri.Builder()
                        builder.scheme(SCHEME_TWIDERE)
                        builder.authority(AUTHORITY_USER_LIST_MEMBERS)
                        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments[1])
                        return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                    }
                    "subscribers" -> {
                        val builder = Uri.Builder()
                        builder.scheme(SCHEME_TWIDERE)
                        builder.authority(AUTHORITY_USER_LIST_SUBSCRIBERS)
                        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments[1])
                        return Pair.create(Intent(Intent.ACTION_VIEW, builder.build()), true)
                    }
                }
            }
        }
        return Pair.create<Intent, Boolean>(null, false)
    }

    private fun getTwitterIntentUriIntent(uri: Uri, pathSegments: List<String>): Pair<Intent, Boolean> {
        if (pathSegments.size < 2) return Pair.create<Intent, Boolean>(null, false)
        when (pathSegments[1]) {
            "tweet" -> {
                val handledIntent = Intent(this, ComposeActivity::class.java)
                handledIntent.action = Intent.ACTION_SEND
                val text = uri.getQueryParameter("text")
                val url = uri.getQueryParameter("url")
                handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url))
                return Pair.create(handledIntent, true)
            }
        }
        return Pair.create<Intent, Boolean>(null, false)
    }

    private fun getIUriIntent(uri: Uri, pathSegments: List<String>): Pair<Intent, Boolean> {
        return Pair.create<Intent, Boolean>(null, false)
    }

    private inner class TwitterLinkException(s: String) : Exception(s)

    companion object {

        val TWITTER_RESERVED_PATHS = arrayOf("about", "account", "accounts", "activity", "all", "announcements", "anywhere", "api_rules", "api_terms", "apirules", "apps", "auth", "badges", "blog", "business", "buttons", "contacts", "devices", "direct_messages", "download", "downloads", "edit_announcements", "faq", "favorites", "find_sources", "find_users", "followers", "following", "friend_request", "friendrequest", "friends", "goodies", "help", "home", "im_account", "inbox", "invitations", "invite", "jobs", "list", "login", "logo", "logout", "me", "mentions", "messages", "mockview", "newtwitter", "notifications", "nudge", "oauth", "phoenix_search", "positions", "privacy", "public_timeline", "related_tweets", "replies", "retweeted_of_mine", "retweets", "retweets_by_others", "rules", "saved_searches", "search", "sent", "settings", "share", "signup", "signin", "similar_to", "statistics", "terms", "tos", "translate", "trends", "tweetbutton", "twttr", "update_discoverability", "users", "welcome", "who_to_follow", "widgets", "zendesk_auth", "media_signup")

        val FANFOU_RESERVED_PATHS = arrayOf("home", "privatemsg", "finder", "browse", "search", "settings", "message", "mentions", "favorites", "friends", "followers", "sharer", "photo", "album", "paipai", "q", "userview", "dialogue")


        private val AUTHORITY_TWITTER_COM = "twitter.com"


        private fun regulateTwitterUri(data: Uri): Uri {
            val encodedFragment = data.encodedFragment
            if (encodedFragment != null && encodedFragment.startsWith("!/")) {
                return regulateTwitterUri(Uri.parse("https://twitter.com" + encodedFragment.substring(1)))
            }
            val builder = data.buildUpon()
            builder.scheme("https")
            builder.authority(AUTHORITY_TWITTER_COM)
            return builder.build()
        }
    }
}
