package org.mariotaku.twidere.constant

import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import androidx.collection.ArraySet
import androidx.core.os.LocaleHelperAccessor
import org.mariotaku.kpreferences.*
import org.mariotaku.ktextension.bcp47Tag
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.TwidereConstants.KEY_MEDIA_PRELOAD
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.annotation.NavbarStyle
import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.extension.getNonEmptyString
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.timeline.UserTimelineFilter
import org.mariotaku.twidere.preference.ThemeBackgroundPreference
import org.mariotaku.twidere.util.sync.DataSyncProvider
import java.util.*

/**
 * Created by mariotaku on 16/8/25.
 */
val textSizeKey = KIntKey(KEY_TEXT_SIZE, 15)
val nameFirstKey = KBooleanKey(KEY_NAME_FIRST, true)
val displayProfileImageKey = KBooleanKey(KEY_DISPLAY_PROFILE_IMAGE, true)
val mediaPreviewKey = KBooleanKey(KEY_MEDIA_PREVIEW, true)
val bandwidthSavingModeKey = KBooleanKey(KEY_BANDWIDTH_SAVING_MODE, false)
val displaySensitiveContentsKey = KBooleanKey(KEY_DISPLAY_SENSITIVE_CONTENTS, false)
val hideCardActionsKey = KBooleanKey(KEY_HIDE_CARD_ACTIONS, false)
val iWantMyStarsBackKey = KBooleanKey(KEY_I_WANT_MY_STARS_BACK, false)
val favoriteConfirmationKey = KBooleanKey(KEY_FAVORITE_CONFIRMATION, false)
val showAbsoluteTimeKey = KBooleanKey(KEY_SHOW_ABSOLUTE_TIME, false)
val statusShortenerKey = KNullableStringKey(KEY_STATUS_SHORTENER, null)
val mediaUploaderKey = KNullableStringKey(KEY_MEDIA_UPLOADER, null)
val newDocumentApiKey = KBooleanKey(KEY_NEW_DOCUMENT_API, Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
val rememberPositionKey = KBooleanKey(KEY_REMEMBER_POSITION, true)
val attachLocationKey = KBooleanKey(KEY_ATTACH_LOCATION, false)
val attachPreciseLocationKey = KBooleanKey(KEY_ATTACH_PRECISE_LOCATION, false)
val noCloseAfterTweetSentKey = KBooleanKey(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
val loadItemLimitKey = KIntKey(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT, 10..200)
val databaseItemLimitKey = KIntKey(KEY_DATABASE_ITEM_LIMIT, DEFAULT_DATABASE_ITEM_LIMIT)
val defaultFeatureLastUpdated = KLongKey("default_feature_last_updated", -1)
val drawerTutorialCompleted = KBooleanKey(KEY_SETTINGS_WIZARD_COMPLETED, false)
val stopAutoRefreshWhenBatteryLowKey = KBooleanKey(KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW, true)
val apiLastChangeKey = KLongKey(KEY_API_LAST_CHANGE, -1)
val bugReportsKey = KBooleanKey(KEY_BUG_REPORTS, BuildConfig.DEBUG)
val readFromBottomKey = KBooleanKey(KEY_READ_FROM_BOTTOM, false)
val randomizeAccountNameKey = KBooleanKey(KEY_RANDOMIZE_ACCOUNT_NAME, false)
val defaultAutoRefreshKey = KBooleanKey(KEY_DEFAULT_AUTO_REFRESH, false)
val defaultAutoRefreshAskedKey = KBooleanKey("default_auto_refresh_asked", true)
val unreadCountKey = KBooleanKey(KEY_UNREAD_COUNT, true)
val drawerToggleKey = KBooleanKey(KEY_DRAWER_TOGGLE, false)
val fabVisibleKey = KBooleanKey(KEY_FAB_VISIBLE, true)
val themeKey = KStringKey(KEY_THEME, VALUE_THEME_NAME_LIGHT)
val themeColorKey = KIntKey(KEY_THEME_COLOR, 0)
val filterUnavailableQuoteStatusesKey = KBooleanKey(KEY_FILTER_UNAVAILABLE_QUOTE_STATUSES, false)
val filterPossibilitySensitiveStatusesKey = KBooleanKey(KEY_FILTER_POSSIBILITY_SENSITIVE_STATUSES, false)
val chromeCustomTabKey = KBooleanKey(KEY_CHROME_CUSTOM_TAB, true)
val usageStatisticsKey = KBooleanKey(KEY_USAGE_STATISTICS, false)
val lightFontKey = KBooleanKey(KEY_LIGHT_FONT, false)
val extraFeaturesNoticeVersionKey = KIntKey("extra_features_notice_version", 0)
val mediaPreloadKey = KBooleanKey(KEY_MEDIA_PRELOAD, false)
val mediaPreloadOnWifiOnlyKey = KBooleanKey(KEY_PRELOAD_WIFI_ONLY, true)
val autoRefreshCompatibilityModeKey = KBooleanKey(KEY_AUTO_REFRESH_COMPATIBILITY_MODE,
        Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
val floatingDetailedContentsKey = KBooleanKey(KEY_FLOATING_DETAILED_CONTENTS, true)
val localTrendsWoeIdKey = KIntKey(KEY_LOCAL_TRENDS_WOEID, 1)
val phishingLinksWaringKey = KBooleanKey(KEY_PHISHING_LINK_WARNING, true)
val multiColumnWidthKey = KStringKey(KEY_MULTI_COLUMN_TAB_WIDTH, "normal")
val quickSendKey = KBooleanKey(KEY_QUICK_SEND, false)
val refreshAfterTweetKey = KBooleanKey(KEY_REFRESH_AFTER_TWEET, false)
val refreshOnStartKey = KBooleanKey(KEY_REFRESH_ON_START, false)
val homeRefreshMentionsKey = KBooleanKey(KEY_HOME_REFRESH_MENTIONS, true)
val homeRefreshDirectMessagesKey = KBooleanKey(KEY_HOME_REFRESH_DIRECT_MESSAGES, true)
val homeRefreshSavedSearchesKey = KBooleanKey(KEY_HOME_REFRESH_SAVED_SEARCHES, true)
val composeStatusVisibilityKey = KNullableStringKey("compose_status_visibility", null)
val navbarStyleKey = KStringKey(KEY_NAVBAR_STYLE, NavbarStyle.DEFAULT)
val lastLaunchTimeKey = KLongKey("last_launch_time", -1)
val promotionsEnabledKey = KBooleanKey("promotions_enabled", false)
val translationDestinationKey = KNullableStringKey(KEY_TRANSLATION_DESTINATION, null)
val tabPositionKey = KStringKey(KEY_TAB_POSITION, SharedPreferenceConstants.DEFAULT_TAB_POSITION)
val autoHideTabs = KBooleanKey(SharedPreferenceConstants.KEY_AUTO_HIDE_TABS, true)
val hideCardNumbersKey = KBooleanKey(KEY_HIDE_CARD_NUMBERS, false)
val showLinkPreviewKey = KBooleanKey(KEY_SHOW_LINK_PREVIEW, false)


object cacheSizeLimitKey : KSimpleKey<Int>(KEY_CACHE_SIZE_LIMIT, 300) {
    override fun read(preferences: SharedPreferences) = preferences.getInt(key, def).coerceIn(100,
            500)

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putInt(key, value)
        return true
    }

}

object overrideLanguageKey : KSimpleKey<Locale?>(KEY_OVERRIDE_LANGUAGE, null) {
    override fun read(preferences: SharedPreferences): Locale? {
        return preferences.getString(key, null)?.takeIf(String::isNotEmpty)
                ?.let(LocaleHelperAccessor::forLanguageTag)
    }

    override fun write(editor: SharedPreferences.Editor, value: Locale?): Boolean {
        editor.putString(key, value?.bcp47Tag)
        return true
    }

}

val themeBackgroundOptionKey = KStringKey(KEY_THEME_BACKGROUND, VALUE_THEME_BACKGROUND_DEFAULT)

object themeBackgroundAlphaKey : KSimpleKey<Int>(KEY_THEME_BACKGROUND_ALPHA, 0xFF) {
    override fun read(preferences: SharedPreferences): Int {
        return preferences.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA)
                .coerceIn(ThemeBackgroundPreference.MIN_ALPHA, ThemeBackgroundPreference.MAX_ALPHA)
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putInt(key, value.coerceIn(ThemeBackgroundPreference.MIN_ALPHA,
                ThemeBackgroundPreference.MAX_ALPHA))
        return true
    }
}

object profileImageStyleKey : KSimpleKey<Int>(KEY_PROFILE_IMAGE_STYLE, ImageShapeStyle.SHAPE_CIRCLE) {
    override fun read(preferences: SharedPreferences): Int {
        if (preferences.getString(key, null) == VALUE_PROFILE_IMAGE_STYLE_SQUARE) return ImageShapeStyle.SHAPE_RECTANGLE
        return ImageShapeStyle.SHAPE_CIRCLE
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, if (value == ImageShapeStyle.SHAPE_CIRCLE) VALUE_PROFILE_IMAGE_STYLE_ROUND else VALUE_PROFILE_IMAGE_STYLE_SQUARE)
        return true
    }

}

object mediaPreviewStyleKey : KSimpleKey<Int>(KEY_MEDIA_PREVIEW_STYLE, PreviewStyle.CROP) {
    override fun read(preferences: SharedPreferences): Int {
        return when (preferences.getString(key, null)) {
            VALUE_MEDIA_PREVIEW_STYLE_SCALE -> PreviewStyle.SCALE
            VALUE_MEDIA_PREVIEW_STYLE_REAL_SIZE -> PreviewStyle.ACTUAL_SIZE
            else -> PreviewStyle.CROP
        }
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, when (value) {
            PreviewStyle.SCALE -> VALUE_MEDIA_PREVIEW_STYLE_SCALE
            PreviewStyle.ACTUAL_SIZE -> VALUE_MEDIA_PREVIEW_STYLE_REAL_SIZE
            else -> VALUE_MEDIA_PREVIEW_STYLE_CROP
        })
        return true
    }

}

object linkHighlightOptionKey : KSimpleKey<Int>(KEY_LINK_HIGHLIGHT_OPTION, VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
    override fun read(preferences: SharedPreferences): Int = when (preferences.getString(key, null)) {
        VALUE_LINK_HIGHLIGHT_OPTION_BOTH -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH
        VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE
        VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
        else -> VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, when (value) {
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH -> VALUE_LINK_HIGHLIGHT_OPTION_BOTH
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE -> VALUE_LINK_HIGHLIGHT_OPTION_UNDERLINE
            VALUE_LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT -> VALUE_LINK_HIGHLIGHT_OPTION_HIGHLIGHT
            else -> VALUE_LINK_HIGHLIGHT_OPTION_NONE
        })
        return true
    }

}

object refreshIntervalKey : KSimpleKey<Long>(KEY_REFRESH_INTERVAL, 15) {
    override fun read(preferences: SharedPreferences): Long {
        return preferences.getString(key, null).toLongOr(def)
    }

    override fun write(editor: SharedPreferences.Editor, value: Long): Boolean {
        editor.putString(key, value.toString())
        return true
    }

}

object defaultAPIConfigKey : KPreferenceKey<CustomAPIConfig> {
    override fun contains(preferences: SharedPreferences): Boolean {
        if (preferences.getString(KEY_API_URL_FORMAT, null) == null) return false
        return true
    }

    override fun read(preferences: SharedPreferences): CustomAPIConfig {
        val apiUrlFormat = preferences.getNonEmptyString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getString(KEY_CREDENTIALS_TYPE, Credentials.Type.OAUTH)
        val customApiType = preferences.getString(KEY_CUSTOM_API_TYPE, null) ?: AccountType.TWITTER
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = preferences.getNonEmptyString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY).trim()
        val consumerSecret = preferences.getNonEmptyString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET).trim()
        return CustomAPIConfig("Default", customApiType, apiUrlFormat, authType, sameOAuthSigningUrl,
                noVersionSuffix, consumerKey, consumerSecret).apply {
            isDefault = true
        }
    }

    override fun write(editor: SharedPreferences.Editor, value: CustomAPIConfig): Boolean {
        if (!TextUtils.isEmpty(value.consumerKey) && !TextUtils.isEmpty(value.consumerSecret)) {
            editor.putString(KEY_CONSUMER_KEY, value.consumerKey)
            editor.putString(KEY_CONSUMER_SECRET, value.consumerSecret)
        } else {
            editor.remove(KEY_CONSUMER_KEY)
            editor.remove(KEY_CONSUMER_SECRET)
        }
        editor.putString(KEY_API_URL_FORMAT, value.apiUrlFormat)
        editor.putString(KEY_CUSTOM_API_TYPE, value.type)
        editor.putString(KEY_CREDENTIALS_TYPE, value.credentialsType)
        editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, value.isSameOAuthUrl)
        editor.putBoolean(KEY_NO_VERSION_SUFFIX, value.isNoVersionSuffix)
        return true
    }

}

object dataSyncProviderInfoKey : KPreferenceKey<DataSyncProvider?> {
    private const val PROVIDER_TYPE_KEY = "sync_provider_type"

    override fun contains(preferences: SharedPreferences): Boolean {
        return read(preferences) != null
    }

    override fun read(preferences: SharedPreferences): DataSyncProvider? {
        val type = preferences.getString(PROVIDER_TYPE_KEY, null) ?: return null
        return DataSyncProvider.Factory.createForType(type, preferences)
    }

    override fun write(editor: SharedPreferences.Editor, value: DataSyncProvider?): Boolean {
        if (value == null) {
            editor.remove(PROVIDER_TYPE_KEY)
        } else {
            editor.putString(PROVIDER_TYPE_KEY, value.type)
            value.writeToPreferences(editor)
        }
        return true
    }

}

object composeAccountsKey : KSimpleKey<Array<UserKey>?>(KEY_COMPOSE_ACCOUNTS, null) {

    override fun read(preferences: SharedPreferences): Array<UserKey>? {
        val string = preferences.getString(key, null) ?: return null
        return UserKey.arrayOf(string)
    }

    override fun write(editor: SharedPreferences.Editor, value: Array<UserKey>?): Boolean {
        editor.putString(key, value?.joinToString(","))
        return true
    }

}

object defaultAccountKey : KSimpleKey<UserKey?>(KEY_DEFAULT_ACCOUNT_KEY, null) {
    override fun read(preferences: SharedPreferences): UserKey? {
        return preferences.getString(key, null)?.let(UserKey::valueOf)
    }

    override fun write(editor: SharedPreferences.Editor, value: UserKey?): Boolean {
        editor.putString(key, value?.toString())
        return true
    }
}

object userTimelineFilterKey : KSimpleKey<UserTimelineFilter>("user_timeline_filter", UserTimelineFilter()) {
    override fun read(preferences: SharedPreferences): UserTimelineFilter {
        val rawString = preferences.getString(key, null) ?: return def
        val options = rawString.split(",")
        return UserTimelineFilter().apply {
            isIncludeReplies = "replies" in options
            isIncludeRetweets = "retweets" in options
        }
    }

    override fun write(editor: SharedPreferences.Editor, value: UserTimelineFilter): Boolean {
        val options = ArraySet<String>().apply {
            if (value.isIncludeReplies) {
                add("replies")
            }
            if (value.isIncludeRetweets) {
                add("retweets")
            }
        }.joinToString(",")
        editor.putString(key, options)
        return true
    }

}