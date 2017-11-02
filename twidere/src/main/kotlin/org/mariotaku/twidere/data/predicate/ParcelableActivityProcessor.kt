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

package org.mariotaku.twidere.data.predicate

import android.content.ContentResolver
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.data.CursorObjectLivePagedListProvider
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.util.getFilteredKeywords
import org.mariotaku.twidere.util.getFilteredUserKeys


class ParcelableActivityProcessor(
        @FilterScope val filterScope: Int,
        val followingOnly: Boolean,
        val mentionsOnly: Boolean
) : CursorObjectLivePagedListProvider.CursorObjectProcessor<ParcelableActivity> {
    private var filteredUserKeys: Array<UserKey>? = null
    private var filteredNameKeywords: Array<String>? = null
    private var filteredDescriptionKeywords: Array<String>? = null

    override fun init(resolver: ContentResolver) {
        filteredUserKeys = resolver.getFilteredUserKeys(filterScope)
        filteredNameKeywords = resolver.getFilteredKeywords(filterScope or FilterScope.TARGET_NAME)
        filteredDescriptionKeywords = resolver.getFilteredKeywords(filterScope or FilterScope.TARGET_DESCRIPTION)
    }

    override fun invalidate() {
    }

    override fun process(obj: ParcelableActivity): ParcelableActivity? {
        if (mentionsOnly && obj.action !in Activity.Action.MENTION_ACTIONS) return null
        val sources = ParcelableActivityUtils.filterSources(obj.sources_lite, filteredUserKeys,
                filteredNameKeywords, filteredDescriptionKeywords, followingOnly)
        obj.after_filtered_sources = sources
        if (sources.isNullOrEmpty()) return null
        return obj
    }

}
