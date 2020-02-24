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

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_URI
import org.mariotaku.twidere.constant.phishingLinksWaringKey
import org.mariotaku.twidere.fragment.PhishingLinkWarningDialogFragment
import org.mariotaku.twidere.model.UserKey

class DirectMessageOnLinkClickHandler(
        context: Context,
        manager: MultiSelectManager?,
        preferences: SharedPreferences
) : OnLinkClickHandler(context, manager, preferences) {

    override val isPrivateData: Boolean
        get() = true

    override fun openLink(accountKey: UserKey?, link: String) {
        if (manager != null && manager.isActive) return
        if (!hasShortenedLinks(link)) {
            super.openLink(accountKey, link)
            return
        }
        if (context is FragmentActivity && preferences[phishingLinksWaringKey]) {
            val fm = context.supportFragmentManager
            val fragment = PhishingLinkWarningDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_URI, Uri.parse(link))
            fragment.arguments = args
            fragment.show(fm, "phishing_link_warning")
        } else {
            super.openLink(accountKey, link)
        }

    }

    private fun hasShortenedLinks(link: String): Boolean {
        for (shortLinkService in SHORT_LINK_SERVICES) {
            if (link.contains(shortLinkService)) return true
        }
        return false
    }

    companion object {

        private val SHORT_LINK_SERVICES = arrayOf("bit.ly", "ow.ly", "tinyurl.com", "goo.gl", "k6.kz", "is.gd", "tr.im", "x.co", "weepp.ru")
    }
}
