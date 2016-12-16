package org.mariotaku.twidere.constant

import android.content.SharedPreferences
import android.os.Build
import android.text.TextUtils
import org.mariotaku.kpreferences.*
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.Constants.KEY_NO_CLOSE_AFTER_TWEET_SENT
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.extension.getNonEmptyString
import org.mariotaku.twidere.model.CustomAPIConfig
import org.mariotaku.twidere.model.account.cred.Credentials

/**
 * Created by mariotaku on 16/8/25.
 */

val mediaPreviewStyleKey = KStringKey(KEY_MEDIA_PREVIEW_STYLE, VALUE_MEDIA_PREVIEW_STYLE_CROP)
val profileImageStyleKey = KStringKey(KEY_PROFILE_IMAGE_STYLE, VALUE_PROFILE_IMAGE_STYLE_ROUND)
val textSizeKey = KIntKey(KEY_TEXT_SIZE, 15)
val nameFirstKey = KBooleanKey(KEY_NAME_FIRST, true)
val displayProfileImageKey = KBooleanKey(KEY_DISPLAY_PROFILE_IMAGE, true)
val mediaPreviewKey = KBooleanKey(KEY_MEDIA_PREVIEW, true)
val bandwidthSavingModeKey = KBooleanKey(KEY_BANDWIDTH_SAVING_MODE, false)
val displaySensitiveContentsKey = KBooleanKey(KEY_DISPLAY_SENSITIVE_CONTENTS, false)
val hideCardActionsKey = KBooleanKey(KEY_HIDE_CARD_ACTIONS, false)
val iWantMyStarsBackKey = KBooleanKey(KEY_I_WANT_MY_STARS_BACK, false)
val showAbsoluteTimeKey = KBooleanKey(KEY_SHOW_ABSOLUTE_TIME, false)
val linkHighlightOptionKey = KStringKey(KEY_LINK_HIGHLIGHT_OPTION, VALUE_LINK_HIGHLIGHT_OPTION_NONE)
val statusShortenerKey = KNullableStringKey(KEY_STATUS_SHORTENER, null)
val mediaUploaderKey = KNullableStringKey(KEY_MEDIA_UPLOADER, null)
val newDocumentApiKey = KBooleanKey(KEY_NEW_DOCUMENT_API, Build.VERSION.SDK_INT == Build.VERSION_CODES.M)
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

object defaultAPIConfigKey : KPreferenceKey<CustomAPIConfig> {
    override fun contains(preferences: SharedPreferences): Boolean {
        if (preferences.getString(KEY_API_URL_FORMAT, null) == null) return false
        return true
    }

    override fun read(preferences: SharedPreferences): CustomAPIConfig {
        val apiUrlFormat = preferences.getNonEmptyString(KEY_API_URL_FORMAT, DEFAULT_TWITTER_API_URL_FORMAT)
        val authType = preferences.getString(KEY_CREDENTIALS_TYPE, Credentials.Type.OAUTH)
        val sameOAuthSigningUrl = preferences.getBoolean(KEY_SAME_OAUTH_SIGNING_URL, false)
        val noVersionSuffix = preferences.getBoolean(KEY_NO_VERSION_SUFFIX, false)
        val consumerKey = preferences.getNonEmptyString(KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY).trim()
        val consumerSecret = preferences.getNonEmptyString(KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET).trim()
        return CustomAPIConfig("Default", apiUrlFormat, authType, sameOAuthSigningUrl,
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
        editor.putString(KEY_CREDENTIALS_TYPE, value.credentialsType)
        editor.putBoolean(KEY_SAME_OAUTH_SIGNING_URL, value.isSameOAuthUrl)
        editor.putBoolean(KEY_NO_VERSION_SUFFIX, value.isNoVersionSuffix)
        return true
    }

}