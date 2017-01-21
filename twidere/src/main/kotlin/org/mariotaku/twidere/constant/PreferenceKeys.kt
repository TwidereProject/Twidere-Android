package org.mariotaku.twidere.constant

import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import org.mariotaku.kpreferences.*
import org.mariotaku.ktextension.toLong
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.KEY_DISPLAY_PROFILE_IMAGE
import org.mariotaku.twidere.Constants.KEY_NO_CLOSE_AFTER_TWEET_SENT
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_COMPOSE_ACCOUNTS
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS
import org.mariotaku.twidere.extension.getNonEmptyString
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.model.sync.SyncProviderInfo
import org.mariotaku.twidere.preference.ThemeBackgroundPreference
import org.mariotaku.twidere.util.sync.SyncProviderInfoFactory
import org.mariotaku.twidere.view.ProfileImageView

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
val showAbsoluteTimeKey = KBooleanKey(KEY_SHOW_ABSOLUTE_TIME, false)
val statusShortenerKey = KNullableStringKey(KEY_STATUS_SHORTENER, null)
val mediaUploaderKey = KNullableStringKey(KEY_MEDIA_UPLOADER, null)
val newDocumentApiKey = KBooleanKey(KEY_NEW_DOCUMENT_API, Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
val rememberPositionKey = KBooleanKey(KEY_REMEMBER_POSITION, true)
val attachLocationKey = KBooleanKey(KEY_ATTACH_LOCATION, false)
val attachPreciseLocationKey = KBooleanKey(KEY_ATTACH_PRECISE_LOCATION, false)
val noCloseAfterTweetSentKey = KBooleanKey(KEY_NO_CLOSE_AFTER_TWEET_SENT, false)
val loadItemLimitKey = KIntKey(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT)
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
val filterUnavailableQuoteStatusesKey = KBooleanKey("filter_unavailable_quote_statuses", false)
val filterPossibilitySensitiveStatusesKey = KBooleanKey("filter_possibility_sensitive_statuses", false)
val chromeCustomTabKey = KBooleanKey("chrome_custom_tab", true)

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

object profileImageStyleKey : KSimpleKey<Int>(KEY_PROFILE_IMAGE_STYLE, ProfileImageView.SHAPE_CIRCLE) {
    override fun read(preferences: SharedPreferences): Int {
        if (preferences.getString(key, null) == VALUE_PROFILE_IMAGE_STYLE_SQUARE) return ProfileImageView.SHAPE_RECTANGLE
        return ProfileImageView.SHAPE_CIRCLE
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, if (value == ProfileImageView.SHAPE_CIRCLE) VALUE_PROFILE_IMAGE_STYLE_ROUND else VALUE_PROFILE_IMAGE_STYLE_SQUARE)
        return true
    }

}

object mediaPreviewStyleKey : KSimpleKey<Int>(KEY_MEDIA_PREVIEW_STYLE, VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP) {
    override fun read(preferences: SharedPreferences): Int {
        if (preferences.getString(key, null) == VALUE_MEDIA_PREVIEW_STYLE_SCALE) return VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE
        return VALUE_MEDIA_PREVIEW_STYLE_CODE_CROP
    }

    override fun write(editor: SharedPreferences.Editor, value: Int): Boolean {
        editor.putString(key, if (value == VALUE_MEDIA_PREVIEW_STYLE_CODE_SCALE) VALUE_MEDIA_PREVIEW_STYLE_SCALE else VALUE_MEDIA_PREVIEW_STYLE_CROP)
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
        return preferences.getString(key, null).toLong(def)
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
                noVersionSuffix, consumerKey, consumerSecret)
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

object dataSyncProviderInfoKey : KPreferenceKey<SyncProviderInfo?> {
    private const val PROVIDER_TYPE_KEY = "sync_provider_type"

    override fun contains(preferences: SharedPreferences): Boolean {
        return read(preferences) != null
    }

    override fun read(preferences: SharedPreferences): SyncProviderInfo? {
        val type = preferences.getString(PROVIDER_TYPE_KEY, null) ?: return null
        return SyncProviderInfoFactory.getInfoForType(type, preferences)
    }

    override fun write(editor: SharedPreferences.Editor, value: SyncProviderInfo?): Boolean {
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