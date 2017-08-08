package org.mariotaku.twidere.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.chromeCustomTabKey
import org.mariotaku.twidere.fragment.SensitiveContentWarningDialogFragment
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableLocationUtils
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.LinkCreator.getTwidereUserListRelatedLink
import java.util.*

/**
 * Created by mariotaku on 16/1/2.
 */
object IntentUtils {

    fun getStatusShareText(context: Context, status: ParcelableStatus): String {
        val link = LinkCreator.getStatusWebLink(status)
        return context.getString(R.string.status_share_text_format_with_link,
                status.text_plain, link.toString())
    }

    fun getStatusShareSubject(context: Context, status: ParcelableStatus): String {
        val timeString = Utils.formatToLongTimeString(context, status.timestamp)
        return context.getString(R.string.status_share_subject_format_with_time,
                status.user_name, status.user_screen_name, timeString)
    }

    fun openUserProfile(context: Context, user: ParcelableUser, newDocument: Boolean,
            @Referral referral: String? = null, activityOptions: Bundle? = null) {
        val intent = userProfile(user, referral)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        ActivityCompat.startActivity(context, intent, activityOptions)
    }

    fun openUserProfile(context: Context, accountKey: UserKey?,
            userKey: UserKey?, screenName: String?, profileUrl: String?,
            newDocument: Boolean, @Referral referral: String? = null,
            activityOptions: Bundle? = null) {
        val intent = userProfile(accountKey, userKey, screenName, referral, profileUrl)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && newDocument) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        ActivityCompat.startActivity(context, intent, activityOptions)
    }


    fun userProfile(user: ParcelableUser, @Referral referral: String? = null): Intent {
        val uri = LinkCreator.getTwidereUserLink(user.account_key, user.key, user.screen_name)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        intent.putExtra(EXTRA_USER, user)
        if (user.extras != null) {
            intent.putExtra(EXTRA_PROFILE_URL, user.extras?.statusnet_profile_url)
        }
        if (referral != null) {
            intent.putExtra(EXTRA_REFERRAL, referral)
        }
        return intent
    }

    fun userProfile(accountKey: UserKey?, userKey: UserKey?, screenName: String?,
            @Referral referral: String? = null, profileUrl: String? = null,
            accountHost: String? = accountKey?.host ?: userKey?.host): Intent {
        val uri = LinkCreator.getTwidereUserLink(accountKey, userKey, screenName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (referral != null) {
            intent.putExtra(EXTRA_REFERRAL, referral)
        }
        intent.putExtra(EXTRA_PROFILE_URL, profileUrl)
        intent.putExtra(EXTRA_ACCOUNT_HOST, accountHost)
        return intent
    }

    fun userTimeline(accountKey: UserKey?, userKey: UserKey?, screenName: String?,
            @Referral referral: String? = null, profileUrl: String? = null): Intent {
        val uri = LinkCreator.getTwidereUserRelatedLink(AUTHORITY_USER_TIMELINE, accountKey,
                userKey, screenName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (referral != null) {
            intent.putExtra(EXTRA_REFERRAL, referral)
        }
        intent.putExtra(EXTRA_PROFILE_URL, profileUrl)
        return intent
    }

    fun userFavorites(accountKey: UserKey?, userKey: UserKey?, screenName: String?,
            @Referral referral: String? = null, profileUrl: String? = null): Intent {
        val uri = LinkCreator.getTwidereUserRelatedLink(AUTHORITY_USER_FAVORITES, accountKey,
                userKey, screenName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (referral != null) {
            intent.putExtra(EXTRA_REFERRAL, referral)
        }
        intent.putExtra(EXTRA_PROFILE_URL, profileUrl)
        return intent
    }

    fun openItems(context: Context, items: List<Parcelable>?) {
        if (items == null) return
        val extras = Bundle()
        extras.putParcelableArrayList(EXTRA_ITEMS, ArrayList(items))
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_ITEMS)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.putExtras(extras)
        context.startActivity(intent)
    }

    fun openUserMentions(context: Context, accountKey: UserKey?,
            screenName: String) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_MENTIONS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openMedia(context: Context, status: ParcelableStatus,
            current: ParcelableMedia? = null, newDocument: Boolean,
            displaySensitiveContents: Boolean, options: Bundle? = null) {
        val media = ParcelableMediaUtils.getPrimaryMedia(status) ?: return
        openMedia(context, status.account_key, status.is_possibly_sensitive, status, current,
                media, newDocument, displaySensitiveContents, options)
    }

    fun openMedia(context: Context, accountKey: UserKey?, media: Array<ParcelableMedia>,
            current: ParcelableMedia? = null, isPossiblySensitive: Boolean,
            newDocument: Boolean, displaySensitiveContents: Boolean, options: Bundle? = null) {
        openMedia(context, accountKey, isPossiblySensitive, null, current, media, newDocument,
                displaySensitiveContents, options)
    }

    fun openMedia(context: Context, accountKey: UserKey?, isPossiblySensitive: Boolean,
            status: ParcelableStatus?, current: ParcelableMedia? = null, media: Array<ParcelableMedia>,
            newDocument: Boolean, displaySensitiveContents: Boolean,
            options: Bundle? = null) {
        if (context is FragmentActivity && isPossiblySensitive && !displaySensitiveContents) {
            val fm = context.supportFragmentManager
            val fragment = SensitiveContentWarningDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
            args.putParcelable(EXTRA_CURRENT_MEDIA, current)
            if (status != null) {
                args.putParcelable(EXTRA_STATUS, status)
            }
            args.putParcelableArray(EXTRA_MEDIA, media)
            args.putBundle(EXTRA_ACTIVITY_OPTIONS, options)
            args.putBoolean(EXTRA_NEW_DOCUMENT, newDocument)
            fragment.arguments = args
            fragment.show(fm, "sensitive_content_warning")
        } else {
            openMediaDirectly(context, accountKey, media, current, options, newDocument, status)
        }
    }

    fun openMediaDirectly(context: Context, accountKey: UserKey?, status: ParcelableStatus,
            current: ParcelableMedia, newDocument: Boolean, options: Bundle? = null) {
        val media = ParcelableMediaUtils.getPrimaryMedia(status) ?: return
        openMediaDirectly(context, accountKey, media, current, options, newDocument, status)
    }

    fun getDefaultBrowserPackage(context: Context, uri: Uri, checkHandled: Boolean): String? {
        if (checkHandled && !isWebLinkHandled(context, uri)) {
            return null
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse("${uri.scheme}://")

        return intent.resolveActivity(context.packageManager)?.takeIf {
            it.className != null && it.packageName != "android"
        }?.packageName
    }

    fun isWebLinkHandled(context: Context, uri: Uri): Boolean {
        val filter = getWebLinkIntentFilter(context) ?: return false
        return filter.match(Intent.ACTION_VIEW, null, uri.scheme, uri,
                setOf(Intent.CATEGORY_BROWSABLE), LOGTAG) >= 0
    }

    fun getWebLinkIntentFilter(context: Context): IntentFilter? {
        val testIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/user_name"))
        testIntent.addCategory(Intent.CATEGORY_BROWSABLE)
        testIntent.`package` = context.packageName
        val resolveInfo = context.packageManager.resolveActivity(testIntent,
                PackageManager.GET_RESOLVED_FILTER)
        return resolveInfo?.filter
    }

    fun openMediaDirectly(context: Context, accountKey: UserKey?, media: Array<ParcelableMedia>,
            current: ParcelableMedia? = null, options: Bundle? = null, newDocument: Boolean,
            status: ParcelableStatus? = null, message: ParcelableMessage? = null) {
        val intent = Intent(context, MediaViewerActivity::class.java)
        intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        intent.putExtra(EXTRA_CURRENT_MEDIA, current)
        intent.putExtra(EXTRA_MEDIA, media)
        if (status != null) {
            intent.putExtra(EXTRA_STATUS, status)
            intent.data = getMediaViewerUri("status", status.id, accountKey)
        }
        if (message != null) {
            intent.putExtra(EXTRA_MESSAGE, message)
            intent.data = getMediaViewerUri("message", message.id, accountKey)
        }
        if (newDocument && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
        ActivityCompat.startActivity(context, intent, options)
    }

    fun getMediaViewerUri(type: String, id: String,
            accountKey: UserKey?): Uri {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority("media")
        builder.appendPath(type)
        builder.appendPath(id)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        return builder.build()
    }

    fun openMessageConversation(context: Context, accountKey: UserKey, conversationId: String) {
        context.startActivity(messageConversation(accountKey, conversationId))
    }

    fun messageConversation(accountKey: UserKey, conversationId: String): Intent {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MESSAGES)
        builder.path(PATH_MESSAGES_CONVERSATION)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, conversationId)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        return intent
    }

    fun messageConversationInfo(accountKey: UserKey, conversationId: String): Intent {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MESSAGES)
        builder.path(PATH_MESSAGES_CONVERSATION_INFO)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, conversationId)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        return intent
    }

    fun newMessageConversation(accountKey: UserKey): Intent {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MESSAGES)
        builder.path(PATH_MESSAGES_CONVERSATION_NEW)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        return intent
    }

    fun openIncomingFriendships(context: Context,
            accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_INCOMING_FRIENDSHIPS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openMap(context: Context, latitude: Double, longitude: Double) {
        if (!ParcelableLocationUtils.isValidLocation(latitude, longitude)) return
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MAP)
        builder.appendQueryParameter(QUERY_PARAM_LAT, latitude.toString())
        builder.appendQueryParameter(QUERY_PARAM_LNG, longitude.toString())
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openMutesUsers(context: Context,
            accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MUTES_USERS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openSavedSearches(context: Context, accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_SAVED_SEARCHES)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openSearch(context: Context, accountKey: UserKey?, query: String, type: String? = null) {
        val intent = Intent(Intent.ACTION_VIEW)
        // Some devices cannot process query parameter with hashes well, so add this intent extra
        intent.putExtra(EXTRA_QUERY, query)
        if (accountKey != null) {
            intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        }

        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_SEARCH)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_QUERY, query)
        if (!TextUtils.isEmpty(type)) {
            builder.appendQueryParameter(QUERY_PARAM_TYPE, type)
            intent.putExtra(EXTRA_TYPE, type)
        }
        intent.data = builder.build()

        context.startActivity(intent)
    }

    fun openMastodonSearch(context: Context, accountKey: UserKey?, query: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        // Some devices cannot process query parameter with hashes well, so add this intent extra
        intent.putExtra(EXTRA_QUERY, query)
        if (accountKey != null) {
            intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        }

        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MASTODON_SEARCH)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_QUERY, query)
        intent.data = builder.build()

        context.startActivity(intent)
    }

    fun status(accountKey: UserKey?, statusId: String): Intent {
        val uri = LinkCreator.getTwidereStatusLink(accountKey, statusId)
        return Intent(Intent.ACTION_VIEW, uri)
    }

    fun openStatus(context: Context, accountKey: UserKey?, statusId: String) {
        context.startActivity(status(accountKey, statusId))
    }

    fun openStatus(context: Context, status: ParcelableStatus, activityOptions: Bundle? = null) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_STATUS)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, status.account_key.toString())
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, status.id)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        intent.putExtra(EXTRA_STATUS, status)
        ActivityCompat.startActivity(context, intent, activityOptions)
    }

    fun openStatusFavoriters(context: Context, accountKey: UserKey?,
            statusId: String) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_STATUS_FAVORITERS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, statusId)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        ActivityCompat.startActivity(context, intent, null)
    }

    fun openStatusRetweeters(context: Context, accountKey: UserKey?,
            statusId: String) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_STATUS_RETWEETERS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, statusId)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        ActivityCompat.startActivity(context, intent, null)
    }

    fun openTweetSearch(context: Context, accountKey: UserKey?,
            query: String) {
        openSearch(context, accountKey, query, QUERY_PARAM_VALUE_TWEETS)
    }

    fun openUserBlocks(activity: Activity?, accountKey: UserKey) {
        if (activity == null) return
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_BLOCKS)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        activity.startActivity(intent)
    }

    fun openUserFavorites(context: Context,
            accountKey: UserKey?,
            userKey: UserKey?,
            screenName: String?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_FAVORITES)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)

    }

    fun openUserFollowers(context: Context, accountKey: UserKey?, userKey: UserKey?,
            screenName: String?) {
        val intent = LinkCreator.getTwidereUserRelatedLink(AUTHORITY_USER_FOLLOWERS,
                accountKey, userKey, screenName)
        context.startActivity(Intent(Intent.ACTION_VIEW, intent))
    }

    fun openUserFriends(context: Context,
            accountKey: UserKey?,
            userKey: UserKey?,
            screenName: String?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_FRIENDS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)

    }

    fun openUserListDetails(context: Context, accountKey: UserKey?, listId: String?,
            userKey: UserKey?, screenName: String?, listName: String?) {
        context.startActivity(userListDetails(accountKey, listId, userKey, screenName, listName))
    }

    fun userListDetails(accountKey: UserKey?, listId: String?, userKey: UserKey?,
            screenName: String?, listName: String?): Intent {
        return Intent(Intent.ACTION_VIEW, getTwidereUserListRelatedLink(AUTHORITY_USER_LIST,
                accountKey, listId, userKey, screenName, listName))
    }

    fun userListTimeline(accountKey: UserKey?, listId: String?, userKey: UserKey?,
            screenName: String?, listName: String?): Intent {
        return Intent(Intent.ACTION_VIEW, getTwidereUserListRelatedLink(AUTHORITY_USER_LIST_TIMELINE,
                accountKey, listId, userKey, screenName, listName))
    }

    fun openUserListDetails(context: Context, userList: ParcelableUserList) {
        context.startActivity(userListDetails(userList))
    }

    fun userListDetails(userList: ParcelableUserList): Intent {
        val userKey = userList.user_key
        val listId = userList.id
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_LIST)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, userList.account_key.toString())
        builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        builder.appendQueryParameter(QUERY_PARAM_LIST_ID, listId)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        intent.putExtra(EXTRA_USER_LIST, userList)
        return intent
    }

    fun openGroupDetails(context: Context, group: ParcelableGroup) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_GROUP)
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, group.account_key.toString())
        builder.appendQueryParameter(QUERY_PARAM_GROUP_ID, group.id)
        builder.appendQueryParameter(QUERY_PARAM_GROUP_NAME, group.nickname)
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        intent.putExtra(EXTRA_GROUP, group)
        context.startActivity(intent)
    }

    fun openUserLists(context: Context,
            accountKey: UserKey?,
            userKey: UserKey?,
            screenName: String?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_LISTS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }


    fun openUserGroups(context: Context, accountKey: UserKey?, userKey: UserKey?,
            screenName: String?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_USER_GROUPS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        if (userKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString())
        }
        if (screenName != null) {
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName)
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openDirectMessages(context: Context, accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_MESSAGES)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openInteractions(context: Context, accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_INTERACTIONS)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openPublicTimeline(context: Context, accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_PUBLIC_TIMELINE)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openNetworkPublicTimeline(context: Context, accountKey: UserKey?) {
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_NETWORK_PUBLIC_TIMELINE)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        val intent = Intent(Intent.ACTION_VIEW, builder.build())
        context.startActivity(intent)
    }

    fun openAccountsManager(context: Context) {
        val intent = Intent()
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_ACCOUNTS)
        intent.data = builder.build()
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openDrafts(context: Context) {
        val intent = Intent()
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_DRAFTS)
        intent.data = builder.build()
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun settings(initialTag: String? = null): Intent {
        val intent = Intent()
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE_SETTINGS)
        builder.authority(initialTag.orEmpty())
        intent.data = builder.build()
        intent.`package` = BuildConfig.APPLICATION_ID
        return intent
    }

    fun openProfileEditor(context: Context, accountKey: UserKey?) {
        val intent = Intent()
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_PROFILE_EDITOR)
        if (accountKey != null) {
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        intent.data = builder.build()
        intent.`package` = BuildConfig.APPLICATION_ID
        context.startActivity(intent)
    }

    fun openFilters(context: Context, initialTab: String? = null) {
        val intent = Intent()
        val builder = Uri.Builder()
        builder.scheme(SCHEME_TWIDERE)
        builder.authority(AUTHORITY_FILTERS)
        intent.data = builder.build()
        intent.`package` = BuildConfig.APPLICATION_ID
        intent.putExtra(EXTRA_INITIAL_TAB, initialTab)
        context.startActivity(intent)
    }

    fun browse(context: Context, preferences: SharedPreferences,
            theme: Chameleon.Theme? = Chameleon.getOverrideTheme(context, ChameleonUtils.getActivity(context)),
            uri: Uri, forceBrowser: Boolean = true): Pair<Intent, Bundle?> {
        if (!preferences[chromeCustomTabKey]) {
            val viewIntent = Intent(Intent.ACTION_VIEW, uri)
            viewIntent.addCategory(Intent.CATEGORY_BROWSABLE)
            return Pair(viewIntent, null)
        }
        val builder = CustomTabsIntent.Builder()
        builder.addDefaultShareMenuItem()
        theme?.let { theme ->
            builder.setToolbarColor(theme.colorToolbar)
        }
        val customTabsIntent = builder.build()
        val intent = customTabsIntent.intent
        intent.data = uri
        if (forceBrowser) {
            intent.`package` = getDefaultBrowserPackage(context, uri, false)
        }
        return Pair(intent, customTabsIntent.startAnimationBundle)
    }

    fun applyNewDocument(intent: Intent, enable: Boolean) {
        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
        }
    }
}
