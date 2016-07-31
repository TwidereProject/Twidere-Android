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
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_URI
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_PHISHING_LINK_WARNING
import org.mariotaku.twidere.fragment.PhishingLinkWarningDialogFragment

class DirectMessageOnLinkClickHandler(
        context: Context,
        manager: MultiSelectManager?,
        preferences: SharedPreferencesWrapper
) : OnLinkClickHandler(context, manager, preferences) {

    override val isPrivateData: Boolean
        get() = true

    override fun openLink(link: String) {
        if (manager != null && manager.isActive) return
        if (!hasShortenedLinks(link)) {
            super.openLink(link)
            return
        }
        val prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (context is FragmentActivity && prefs.getBoolean(KEY_PHISHING_LINK_WARNING, true)) {
            val fm = context.supportFragmentManager
            val fragment = PhishingLinkWarningDialogFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_URI, Uri.parse(link))
            fragment.arguments = args
            fragment.show(fm, "phishing_link_warning")
        } else {
            super.openLink(link)
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
