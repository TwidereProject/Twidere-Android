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

package org.mariotaku.twidere.preference

import android.content.Context
import android.util.AttributeSet
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.TIMELINE_POSITIONS_PREFERENCES_NAME
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.util.DataStoreUtils

class ClearDatabasesPreference(context: Context, attrs: AttributeSet? = null) : AsyncTaskPreference(context, attrs, R.attr.preferenceStyle) {

    override fun doInBackground() {
        val context = context ?: return
        val resolver = context.contentResolver
        for (uri in DataStoreUtils.STATUSES_URIS) {
            if (CachedStatuses.CONTENT_URI == uri) {
                continue
            }
            resolver.delete(uri, null, null)
        }
        for (uri in DataStoreUtils.MESSAGES_URIS) {
            resolver.delete(uri, null, null)
        }
        for (uri in DataStoreUtils.CACHE_URIS) {
            resolver.delete(uri, null, null)
        }
        resolver.delete(Activities.AboutMe.CONTENT_URI, null, null)
        resolver.delete(Activities.ByFriends.CONTENT_URI, null, null)
        resolver.delete(Notifications.CONTENT_URI, null, null)
        resolver.delete(UnreadCounts.CONTENT_URI, null, null)
        resolver.delete(SavedSearches.CONTENT_URI, null, null)

        val prefs = context.getSharedPreferences(TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

}
