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

package org.mariotaku.twidere.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.BadParcelableException
import androidx.core.content.ContextCompat
import okhttp3.HttpUrl
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_TWITTER_COM
import org.mariotaku.twidere.activity.WebLinkHandlerActivity
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_HOST
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.displaySensitiveContentsKey
import org.mariotaku.twidere.constant.newDocumentApiKey
import org.mariotaku.twidere.extension.model.AcctPlaceholderUserKey
import org.mariotaku.twidere.extension.toUri
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener
import org.mariotaku.twidere.util.TwidereLinkify.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor

open class OnLinkClickHandler(
        protected val context: Context,
        protected val manager: MultiSelectManager?,
        protected val preferences: SharedPreferences
) : OnLinkClickListener {

    override fun onLinkClick(link: String, orig: String?, accountKey: UserKey?,
            extraId: Long, type: Int, sensitive: Boolean,
            start: Int, end: Int): Boolean {
        if (manager != null && manager.isActive) return false

        when (type) {
            TwidereLinkify.LINK_TYPE_MENTION -> {
                IntentUtils.openUserProfile(context, accountKey, null, link, null,
                        preferences[newDocumentApiKey], null)
                return true
            }
            TwidereLinkify.LINK_TYPE_HASHTAG -> {
                IntentUtils.openTweetSearch(context, accountKey, "#$link")
                return true
            }
            TwidereLinkify.LINK_TYPE_LINK_IN_TEXT -> {
                if (accountKey != null && isMedia(link, extraId)) {
                    openMedia(accountKey, extraId, sensitive, link, start, end)
                } else {
                    openLink(accountKey, link)
                }
                return true
            }
            TwidereLinkify.LINK_TYPE_ENTITY_URL -> {
                if (accountKey != null && isMedia(link, extraId)) {
                    openMedia(accountKey, extraId, sensitive, link, start, end)
                } else {
                    val authority = UriUtils.getAuthority(link)
                    if (authority == "fanfou.com") {
                        if (accountKey != null && handleFanfouLink(link, orig, accountKey)) {
                            return true
                        }
                    } else if (IntentUtils.isWebLinkHandled(context, Uri.parse(link))) {
                        openTwitterLink(accountKey, link)
                        return true
                    }
                    openLink(accountKey, link)
                }
                return true
            }
            TwidereLinkify.LINK_TYPE_LIST -> {
                val mentionList = link.split("/")
                if (mentionList.size != 2) {
                    return false
                }
                IntentUtils.openUserListDetails(context, accountKey, null, null, mentionList[0],
                        mentionList[1])
                return true
            }
            TwidereLinkify.LINK_TYPE_CASHTAG -> {
                IntentUtils.openTweetSearch(context, accountKey, link)
                return true
            }
            TwidereLinkify.LINK_TYPE_USER_ID -> {
                IntentUtils.openUserProfile(context, accountKey, UserKey.valueOf(link), null, null,
                        preferences[newDocumentApiKey], null)
                return true
            }
            TwidereLinkify.LINK_TYPE_USER_ACCT -> {
                val acctKey = UserKey.valueOf(link)
                IntentUtils.openUserProfile(context, accountKey, AcctPlaceholderUserKey(acctKey.host),
                        acctKey.id, null, preferences[newDocumentApiKey], null)
                return true
            }
        }
        return false
    }

    protected open val isPrivateData: Boolean
        get() = false

    protected open fun isMedia(link: String, extraId: Long): Boolean {
        return PreviewMediaExtractor.isSupported(link)
    }

    protected open fun openMedia(accountKey: UserKey, extraId: Long, sensitive: Boolean, link: String, start: Int, end: Int) {
        val media = arrayOf(ParcelableMediaUtils.image(link))
        IntentUtils.openMedia(context, accountKey, media, null, sensitive, preferences[newDocumentApiKey],
                preferences[displaySensitiveContentsKey])
    }

    protected open fun openLink(accountKey: UserKey?, link: String) {
        if (manager != null && manager.isActive) return
        val uri = Uri.parse(link)
        if (uri.isRelative && accountKey != null && accountKey.host != null) {
            val absUri = HttpUrl.parse("http://${accountKey.host}/")?.resolve(link)?.toUri()!!
            openLink(context, preferences, absUri)
            return
        }
        openLink(context, preferences, uri)
    }

    protected fun openTwitterLink(accountKey: UserKey?, link: String) {
        if (manager != null && manager.isActive) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClass(context, WebLinkHandlerActivity::class.java)
        if (accountKey != null && accountKey.host == USER_TYPE_TWITTER_COM) {
            intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        } else {
            intent.putExtra(EXTRA_ACCOUNT_HOST, USER_TYPE_TWITTER_COM)
        }
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
            } catch (e: BadParcelableException) {
                // Ignore
            }

        }
    }

    private fun handleFanfouLink(link: String, orig: String?, accountKey: UserKey): Boolean {
        if (orig == null) return false
        // Process special case for fanfou
        val ch = orig[0]
        // Extend selection
        val length = orig.length
        if (TwidereLinkify.isAtSymbol(ch)) {
            var id = UriUtils.getPath(link)
            if (id != null) {
                val idxOfSlash = id.indexOf('/')
                if (idxOfSlash == 0) {
                    id = id.substring(1)
                }
                val screenName = orig.substring(1, length)
                IntentUtils.openUserProfile(context, accountKey, UserKey.valueOf(id), screenName,
                        null, preferences[newDocumentApiKey], null)
                return true
            }
        } else if (TwidereLinkify.isHashSymbol(ch) && TwidereLinkify.isHashSymbol(orig[length - 1])) {
            IntentUtils.openSearch(context, accountKey, orig.substring(1, length - 1))
            return true
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.setClass(context, WebLinkHandlerActivity::class.java)
        if (accountKey.host == USER_TYPE_FANFOU_COM) {
            intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        }
        context.startActivity(intent)
        return true
    }

    companion object {

        fun openLink(context: Context, preferences: SharedPreferences, uri: Uri) {
            val (intent, options) = IntentUtils.browse(context, preferences, uri = uri,
                    forceBrowser = false)
            try {
                ContextCompat.startActivity(context, intent, options)
            } catch (e: ActivityNotFoundException) {
                Analyzer.logException(e)
                DebugLog.w(tr = e)
            }
        }
    }
}

