/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.Activity
import android.content.*
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.BatteryManager
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.net.ConnectivityManagerCompat
import androidx.core.view.GravityCompat
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.getNullableTypedArray
import org.mariotaku.ktextension.toLocalizedString
import org.mariotaku.pickncrop.library.PNCUtils
import org.mariotaku.sqliteqb.library.AllColumns
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.Selectable
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.TwidereConstants.METADATA_KEY_EXTENSION_USE_JSON
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.TwidereConstants.TAB_CODE_DIRECT_MESSAGES
import org.mariotaku.twidere.TwidereConstants.TAB_CODE_HOME_TIMELINE
import org.mariotaku.twidere.TwidereConstants.TAB_CODE_NOTIFICATIONS_TIMELINE
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.ProfileImageSize
import org.mariotaku.twidere.constant.CompatibilityConstants.EXTRA_ACCOUNT_ID
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEYS
import org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_PEBBLE_NOTIFICATION
import org.mariotaku.twidere.constant.SharedPreferenceConstants.*
import org.mariotaku.twidere.constant.bandwidthSavingModeKey
import org.mariotaku.twidere.constant.defaultAccountKey
import org.mariotaku.twidere.constant.mediaPreviewKey
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.PebbleMessage
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES
import org.mariotaku.twidere.view.TabPagerIndicator
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Pattern

object Utils {

    private val PATTERN_XML_RESOURCE_IDENTIFIER = Pattern.compile("res/xml/([\\w_]+)\\.xml")
    private val PATTERN_RESOURCE_IDENTIFIER = Pattern.compile("@([\\w_]+)/([\\w_]+)")

    private val HOME_TABS_URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)

    init {

        HOME_TABS_URI_MATCHER.addURI(CustomTabType.HOME_TIMELINE, null, TAB_CODE_HOME_TIMELINE)
        HOME_TABS_URI_MATCHER.addURI(CustomTabType.NOTIFICATIONS_TIMELINE, null, TAB_CODE_NOTIFICATIONS_TIMELINE)
        HOME_TABS_URI_MATCHER.addURI(CustomTabType.DIRECT_MESSAGES, null, TAB_CODE_DIRECT_MESSAGES)
    }


    fun announceForAccessibilityCompat(context: Context, view: View, text: CharSequence,
            cls: Class<*>) {
        val accessibilityManager = context
                .getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (!accessibilityManager.isEnabled) return
        // Prior to SDK 16, announcements could only be made through FOCUSED
        // events. Jelly Bean (SDK 16) added support for speaking text verbatim
        // using the ANNOUNCEMENT event type.
        val eventType: Int = AccessibilityEventCompat.TYPE_ANNOUNCEMENT

        // Construct an accessibility event with the minimum recommended
        // attributes. An event without a class name or package may be dropped.
        val event = AccessibilityEvent.obtain(eventType)
        event.text.add(text)
        event.className = cls.name
        event.packageName = context.packageName
        event.setSource(view)

        // Sends the event directly through the accessibility manager. If your
        // application only targets SDK 14+, you should just call
        // getParent().requestSendAccessibilityEvent(this, event);
        accessibilityManager.sendAccessibilityEvent(event)
    }

    fun deleteMedia(context: Context, uri: Uri): Boolean {
        return try {
            PNCUtils.deleteMedia(context, uri)
        } catch (e: SecurityException) {
            false
        }

    }

    fun sanitizeMimeType(contentType: String?): String? {
        if (contentType == null) return null
        when (contentType) {
            "image/jpg" -> return "image/jpeg"
        }
        return contentType
    }

    fun createStatusShareIntent(context: Context, status: ParcelableStatus): Intent {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, IntentUtils.getStatusShareSubject(context, status))
        intent.putExtra(Intent.EXTRA_TEXT, IntentUtils.getStatusShareText(context, status))
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return intent
    }

    fun getAccountKeys(context: Context, args: Bundle?): Array<UserKey>? {
        if (args == null) return null
        when {
            args.containsKey(EXTRA_ACCOUNT_KEYS) -> {
                return args.getNullableTypedArray(EXTRA_ACCOUNT_KEYS)
            }
            args.containsKey(EXTRA_ACCOUNT_KEY) -> {
                val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY) ?: return emptyArray()
                return arrayOf(accountKey)
            }
            args.containsKey(EXTRA_ACCOUNT_ID) -> {
                val accountId = args.get(EXTRA_ACCOUNT_ID).toString()
                try {
                    if (java.lang.Long.parseLong(accountId) <= 0) return null
                } catch (e: NumberFormatException) {
                    // Ignore
                }

                val accountKey = DataStoreUtils.findAccountKey(context, accountId)
                args.putParcelable(EXTRA_ACCOUNT_KEY, accountKey)
                if (accountKey == null) return arrayOf(UserKey(accountId, null))
                return arrayOf(accountKey)
            }
            else -> return null
        }
    }

    fun getAccountKey(context: Context, args: Bundle?): UserKey? {
        return getAccountKeys(context, args)?.firstOrNull()
    }

    fun getReadPositionTagWithAccount(tag: String, accountKey: UserKey?): String {
        if (accountKey == null) return tag
        return "$accountKey:$tag"
    }

    fun formatSameDayTime(context: Context, timestamp: Long): String? {
        if (DateUtils.isToday(timestamp))
            return DateUtils.formatDateTime(context, timestamp,
                    if (DateFormat.is24HourFormat(context))
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_24HOUR
                    else
                        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_12HOUR)
        return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE)
    }

    fun formatToLongTimeString(context: Context, timestamp: Long): String? {
        val formatFlags = DateUtils.FORMAT_NO_NOON_MIDNIGHT or DateUtils.FORMAT_ABBREV_ALL or
                DateUtils.FORMAT_CAP_AMPM or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME
        return DateUtils.formatDateTime(context, timestamp, formatFlags)
    }

    fun isComposeNowSupported(context: Context): Boolean {
        return hasNavBar(context)
    }

    fun setLastSeen(context: Context, entities: Array<ParcelableUserMention>?, time: Long): Boolean {
        if (entities == null) return false
        var result = false
        for (entity in entities) {
            result = result or setLastSeen(context, entity.key, time)
        }
        return result
    }

    fun setLastSeen(context: Context, userKey: UserKey, time: Long): Boolean {
        val cr = context.contentResolver
        val values = ContentValues()
        if (time > 0) {
            values.put(CachedUsers.LAST_SEEN, time)
        } else {
            // Zero or negative value means remove last seen
            values.putNull(CachedUsers.LAST_SEEN)
        }
        val where = Expression.equalsArgs(CachedUsers.USER_KEY).sql
        val selectionArgs = arrayOf(userKey.toString())
        return cr.update(CachedUsers.CONTENT_URI, values, where, selectionArgs) > 0
    }


    fun getColumnsFromProjection(projection: Array<String>?): Selectable {
        if (projection == null) return AllColumns()
        val length = projection.size
        val columns = arrayOfNulls<Column>(length)
        for (i in 0 until length) {
            columns[i] = Column(projection[i])
        }
        return Columns(*columns)
    }

    fun getDefaultAccountKey(context: Context): UserKey? {
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val accountKey = prefs[defaultAccountKey]
        val accountKeys = DataStoreUtils.getAccountKeys(context)

        return accountKeys.find { it.maybeEquals(accountKey) } ?: run {
            if (!accountKeys.contains(accountKey)) {
                return@run accountKeys.firstOrNull()
            }
            return@run null
        }
    }

    fun getInternalCacheDir(context: Context?, cacheDirName: String): File {
        if (context == null) throw NullPointerException()
        val cacheDir = File(context.cacheDir, cacheDirName)
        if (cacheDir.isDirectory || cacheDir.mkdirs()) return cacheDir
        return File(context.cacheDir, cacheDirName)
    }

    fun getExternalCacheDir(context: Context, cacheDirName: String, sizeInBytes: Long): File? {
        val externalCacheDir = try {
            context.externalCacheDir
        } catch (e: SecurityException) {
            null
        } ?: return null
        val cacheDir = File(externalCacheDir, cacheDirName)
        if (sizeInBytes > 0 && externalCacheDir.freeSpace < sizeInBytes / 10) {
            // Less then 10% space available
            return null
        }
        if (cacheDir.isDirectory || cacheDir.mkdirs()) return cacheDir
        return null
    }

    fun getLocalizedNumber(locale: Locale, number: Number): String {
        return number.toLocalizedString(locale)
    }

    fun getMatchedNicknameKeys(str: String, manager: UserColorNameManager): Array<String> {
        if (str.isEmpty()) return emptyArray()
        return manager.nicknames.filter { (_, value) ->
            val valueStr = value?.toString()?.takeIf(String::isNotEmpty) ?: return@filter false
            return@filter valueStr.startsWith(str, ignoreCase = true)
        }.map { it.key }.toTypedArray()
    }

    fun getOriginalTwitterProfileImage(url: String): String {
        val matcher = PATTERN_TWITTER_PROFILE_IMAGES.matcher(url)
        if (matcher.matches())
            return matcher.replaceFirst("$1$2/profile_images/$3/$4$6")
        return url
    }

    fun getQuoteStatus(context: Context?, status: ParcelableStatus): String? {
        if (context == null) return null
        var quoteFormat: String = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString(KEY_QUOTE_FORMAT, DEFAULT_QUOTE_FORMAT).orEmpty()
        if (TextUtils.isEmpty(quoteFormat)) {
            quoteFormat = DEFAULT_QUOTE_FORMAT
        }
        var result = quoteFormat.replace(FORMAT_PATTERN_LINK, LinkCreator.getStatusWebLink(status).toString())
        result = result.replace(FORMAT_PATTERN_NAME, status.user_screen_name)
        result = result.replace(FORMAT_PATTERN_TEXT, status.text_plain)
        return result
    }

    fun getResId(context: Context?, string: String?): Int {
        if (context == null || string == null) return 0
        var m = PATTERN_RESOURCE_IDENTIFIER.matcher(string)
        val res = context.resources
        if (m.matches()) return res.getIdentifier(m.group(2), m.group(1), context.packageName)
        m = PATTERN_XML_RESOURCE_IDENTIFIER.matcher(string)
        if (m.matches()) return res.getIdentifier(m.group(1), "xml", context.packageName)
        return 0
    }


    fun getTabDisplayOption(context: Context): String {
        val defaultOption = context.getString(R.string.default_tab_display_option)
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TAB_DISPLAY_OPTION, defaultOption) ?: defaultOption
    }

    fun getTabDisplayOptionInt(context: Context): Int {
        return getTabDisplayOptionInt(getTabDisplayOption(context))
    }

    fun getTabDisplayOptionInt(option: String): Int {
        if (VALUE_TAB_DISPLAY_OPTION_ICON == option)
            return TabPagerIndicator.DisplayOption.ICON
        else if (VALUE_TAB_DISPLAY_OPTION_LABEL == option)
            return TabPagerIndicator.DisplayOption.LABEL
        return TabPagerIndicator.DisplayOption.BOTH
    }

    fun hasNavBar(context: Context): Boolean {
        val resources = context.resources ?: return false
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (id > 0) {
            resources.getBoolean(id)
        } else {
            // Check for keys
            !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK) && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME)
        }
    }


    fun getTwitterProfileImageOfSize(url: String, size: String): String {
        if (ProfileImageSize.ORIGINAL == size) {
            return getOriginalTwitterProfileImage(url)
        }
        val matcher = PATTERN_TWITTER_PROFILE_IMAGES.matcher(url)
        if (matcher.matches()) {
            return matcher.replaceFirst("$1$2/profile_images/$3/$4_$size$6")
        }
        return url
    }

    @DrawableRes
    fun getUserTypeIconRes(isVerified: Boolean, isProtected: Boolean): Int {
        if (isVerified)
            return R.drawable.ic_user_type_verified
        else if (isProtected) return R.drawable.ic_user_type_protected
        return 0
    }

    @StringRes
    fun getUserTypeDescriptionRes(isVerified: Boolean, isProtected: Boolean): Int {
        if (isVerified)
            return R.string.user_type_verified
        else if (isProtected) return R.string.user_type_protected
        return 0
    }

    fun isBatteryOkay(context: Context?): Boolean {
        if (context == null) return false
        val app = context.applicationContext
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val intent: Intent?
        try {
            intent = app.registerReceiver(null, filter)
        } catch (e: Exception) {
            return false
        }

        if (intent == null) return false
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0).toFloat()
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).toFloat()
        return plugged || level / scale > 0.15f
    }

    fun isMyAccount(context: Context, accountKey: UserKey): Boolean {
        val am = AccountManager.get(context)
        return AccountUtils.findByAccountKey(am, accountKey) != null
    }

    fun isMyAccount(context: Context, screenName: String): Boolean {
        val am = AccountManager.get(context)
        return AccountUtils.findByScreenName(am, screenName) != null
    }

    fun isMyRetweet(status: ParcelableStatus?): Boolean {
        return status != null && isMyRetweet(status.account_key, status.retweeted_by_user_key,
                status.my_retweet_id)
    }

    fun isMyRetweet(accountKey: UserKey, retweetedByKey: UserKey?, myRetweetId: String?): Boolean {
        return accountKey == retweetedByKey || myRetweetId != null
    }

    fun matchTabCode(uri: Uri): Int {
        return HOME_TABS_URI_MATCHER.match(uri)
    }


    @CustomTabType
    fun matchTabType(uri: Uri): String? {
        return getTabType(matchTabCode(uri))
    }

    @CustomTabType
    fun getTabType(code: Int): String? {
        when (code) {
            TAB_CODE_HOME_TIMELINE -> {
                return CustomTabType.HOME_TIMELINE
            }
            TAB_CODE_NOTIFICATIONS_TIMELINE -> {
                return CustomTabType.NOTIFICATIONS_TIMELINE
            }
            TAB_CODE_DIRECT_MESSAGES -> {
                return CustomTabType.DIRECT_MESSAGES
            }
        }
        return null
    }


    fun setNdefPushMessageCallback(activity: Activity, callback: CreateNdefMessageCallback): Boolean {
        try {
            val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return false
            adapter.setNdefPushMessageCallback(callback, activity)
            return true
        } catch (e: SecurityException) {
            Log.w(LOGTAG, e)
        }

        return false
    }

    fun getInsetsTopWithoutActionBarHeight(context: Context, top: Int): Int {
        val actionBarHeight: Int = when (context) {
            is AppCompatActivity -> {
                getActionBarHeight(context.supportActionBar)
            }
            is Activity -> {
                getActionBarHeight(context.actionBar)
            }
            else -> {
                return top
            }
        }
        if (actionBarHeight > top) {
            return top
        }
        return top - actionBarHeight
    }

    fun restartActivity(activity: Activity?) {
        if (activity == null) return
        val enterAnim = android.R.anim.fade_in
        val exitAnim = android.R.anim.fade_out
        activity.finish()
        activity.overridePendingTransition(enterAnim, exitAnim)
        activity.startActivity(activity.intent)
        activity.overridePendingTransition(enterAnim, exitAnim)
    }

    internal fun isMyStatus(status: ParcelableStatus): Boolean {
        if (isMyRetweet(status)) return true
        return status.account_key.maybeEquals(status.user_key)
    }

    fun showMenuItemToast(v: View, text: CharSequence, isBottomBar: Boolean) {
        val screenPos = IntArray(2)
        val displayFrame = Rect()
        v.getLocationOnScreen(screenPos)
        v.getWindowVisibleDisplayFrame(displayFrame)
        val width = v.width
        val height = v.height
        val screenWidth = v.resources.displayMetrics.widthPixels
        val cheatSheet = Toast.makeText(v.context.applicationContext, text, Toast.LENGTH_SHORT)
        if (isBottomBar) {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, height)
        } else {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP or GravityCompat.END, screenWidth - screenPos[0] - width / 2, height)
        }
        cheatSheet.show()
    }

    internal fun getMetadataDrawable(pm: PackageManager?, info: ActivityInfo?, key: String?): Drawable? {
        if (pm == null || info == null || info.metaData == null || key == null || !info.metaData.containsKey(key))
            return null
        return pm.getDrawable(info.packageName, info.metaData.getInt(key), info.applicationInfo)
    }

    internal fun isExtensionUseJSON(info: ResolveInfo?): Boolean {
        if (info?.activityInfo == null) return false
        val activityInfo = info.activityInfo
        if (activityInfo.metaData != null && activityInfo.metaData.containsKey(METADATA_KEY_EXTENSION_USE_JSON))
            return activityInfo.metaData.getBoolean(METADATA_KEY_EXTENSION_USE_JSON)
        val appInfo = activityInfo.applicationInfo ?: return false
        return appInfo.metaData != null && appInfo.metaData.getBoolean(METADATA_KEY_EXTENSION_USE_JSON, false)
    }

    fun getActionBarHeight(actionBar: ActionBar?): Int {
        if (actionBar == null) return 0
        val context = actionBar.themedContext
        val tv = TypedValue()
        val height = actionBar.height
        if (height > 0) return height
        if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return 0
    }

    fun getActionBarHeight(actionBar: androidx.appcompat.app.ActionBar?): Int {
        if (actionBar == null) return 0
        val context = actionBar.themedContext
        val tv = TypedValue()
        val height = actionBar.height
        if (height > 0) return height
        if (context.theme.resolveAttribute(R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, context.resources.displayMetrics)
        }
        return 0
    }

    fun getNotificationId(baseId: Int, accountKey: UserKey?): Int {
        var result = baseId
        result = 31 * result + (accountKey?.hashCode() ?: 0)
        return result
    }

    @SuppressLint("InlinedApi")
    fun isCharging(context: Context?): Boolean {
        if (context == null) return false
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return false
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        return plugged == BatteryManager.BATTERY_PLUGGED_AC
                || plugged == BatteryManager.BATTERY_PLUGGED_USB
                || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    fun isMediaPreviewEnabled(context: Context, preferences: SharedPreferences): Boolean {
        if (!preferences[mediaPreviewKey])
            return false
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return !ConnectivityManagerCompat.isActiveNetworkMetered(cm) || !preferences[bandwidthSavingModeKey]
    }

    /**
     * Send Notifications to Pebble smart watches

     * @param context Context
     * *
     * @param title   String
     * *
     * @param message String
     */
    fun sendPebbleNotification(context: Context, title: String?, message: String) {

        val appName: String = if (title == null) {
            context.getString(R.string.app_name)
        } else {
            "${context.getString(R.string.app_name)} - $title"
        }

        if (TextUtils.isEmpty(message)) return
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        if (prefs.getBoolean(KEY_PEBBLE_NOTIFICATIONS, false)) {

            val messages = ArrayList<PebbleMessage>()
            messages.add(PebbleMessage(appName, message))

            val intent = Intent(INTENT_ACTION_PEBBLE_NOTIFICATION)
            intent.putExtra("messageType", "PEBBLE_ALERT")
            intent.putExtra("sender", appName)
            intent.putExtra("notificationData", JsonSerializer.serializeList(messages, PebbleMessage::class.java))

            context.applicationContext.sendBroadcast(intent)
        }

    }

    fun copyStream(input: InputStream, output: OutputStream, length: Int) {
        val buffer = ByteArray(1024)
        var bytesRead = 0
        do {
            val read = input.read(buffer)
            if (read == -1) {
                break
            }
            output.write(buffer, 0, read)
            bytesRead += read
        } while (bytesRead <= length)
    }
}
