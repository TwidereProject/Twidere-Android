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
import android.net.Uri
import android.os.BadParcelableException
import edu.tsinghua.hotmobi.HotMobiLogger
import edu.tsinghua.hotmobi.model.LinkEvent
import org.apache.commons.lang3.StringUtils
import org.mariotaku.twidere.activity.WebLinkHandlerActivity
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor

open class OnLinkClickHandler(
        protected val context: Context,
        protected val manager: MultiSelectManager?,
        protected val preferences: SharedPreferencesWrapper
) : OnLinkClickListener {

    override fun onLinkClick(link: String, orig: String?, accountKey: UserKey?,
                             extraId: Long, type: Int, sensitive: Boolean,
                             start: Int, end: Int): Boolean {
        if (manager != null && manager.isActive) return false
        if (!isPrivateData) {
            // BEGIN HotMobi
            val event = LinkEvent.create(context, link, type)
            HotMobiLogger.getInstance(context).log(accountKey, event)
            // END HotMobi
        }

        when (type) {
            TwidereLinkify.LINK_TYPE_MENTION -> {
                IntentUtils.openUserProfile(context, accountKey, null, link, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.USER_MENTION)
                return true
            }
            TwidereLinkify.LINK_TYPE_HASHTAG -> {
                IntentUtils.openTweetSearch(context, accountKey, "#" + link)
                return true
            }
            TwidereLinkify.LINK_TYPE_LINK_IN_TEXT -> {
                if (isMedia(link, extraId)) {
                    openMedia(accountKey!!, extraId, sensitive, link, start, end)
                } else {
                    openLink(link)
                }
                return true
            }
            TwidereLinkify.LINK_TYPE_ENTITY_URL -> {
                if (isMedia(link, extraId)) {
                    openMedia(accountKey!!, extraId, sensitive, link, start, end)
                } else {
                    val authority = UriUtils.getAuthority(link)
                    if (authority == null) {
                        openLink(link)
                        return true
                    }
                    when (authority) {
                        "fanfou.com" -> {
                            if (orig != null) {
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
                                        IntentUtils.openUserProfile(context, accountKey, UserKey.valueOf(id),
                                                screenName, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                                                Referral.USER_MENTION)
                                        return true
                                    }
                                } else if (TwidereLinkify.isHashSymbol(ch) && TwidereLinkify.isHashSymbol(orig[length - 1])) {
                                    IntentUtils.openSearch(context, accountKey, orig.substring(1, length - 1))
                                    return true
                                }
                            }
                        }
                        else -> {
                            if (IntentUtils.isWebLinkHandled(context, Uri.parse(link))) {
                                openTwitterLink(link, accountKey!!)
                                return true
                            }
                        }
                    }
                    openLink(link)
                }
                return true
            }
            TwidereLinkify.LINK_TYPE_LIST -> {
                val mentionList = StringUtils.split(link, "/")
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
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.USER_MENTION)
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
        IntentUtils.openMedia(context, accountKey, sensitive, null, media, null,
                preferences.getBoolean(KEY_NEW_DOCUMENT_API))
    }

    protected open fun openLink(link: String) {
        if (manager != null && manager.isActive) return
        val uri = Uri.parse(link)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.`package` = IntentUtils.getDefaultBrowserPackage(context, uri, true)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // TODO
        }

    }

    protected fun openTwitterLink(link: String, accountKey: UserKey) {
        if (manager != null && manager.isActive) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setClass(context, WebLinkHandlerActivity::class.java)
        intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
        intent.setExtrasClassLoader(TwidereApplication::class.java.classLoader)
        if (intent.resolveActivity(context.packageManager) != null) {
            try {
                context.startActivity(intent)
            } catch (e: BadParcelableException) {
                // Ignore
            }

        }
    }
}
