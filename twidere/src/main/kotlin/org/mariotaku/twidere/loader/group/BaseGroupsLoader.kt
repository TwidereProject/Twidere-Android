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

package org.mariotaku.twidere.loader.group

import android.content.Context
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.statusnet.model.Group
import org.mariotaku.microblog.library.twitter.model.PageableResponseList
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.loader.iface.IPaginationLoader
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.CursorPagination
import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.util.ParcelableGroupUtils
import org.mariotaku.twidere.util.DebugLog
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList
import java.util.*


abstract class BaseGroupsLoader(
        context: Context,
        protected val accountKey: UserKey,
        data: List<ParcelableGroup>?
) : FixedAsyncTaskLoader<List<ParcelableGroup>>(context), IPaginationLoader {

    override var pagination: Pagination? = null

    final override var nextPagination: Pagination? = null
        private set

    final override var prevPagination: Pagination? = null
        private set

    protected val data = NoDuplicatesArrayList<ParcelableGroup>()

    init {
        if (data != null) {
            this.data.addAll(data)
        }
    }

    @Throws(MicroBlogException::class)
    abstract fun getGroups(twitter: MicroBlog): List<Group>

    override fun loadInBackground(): List<ParcelableGroup> {
        val twitter = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return emptyList()
        var listLoaded: List<Group>? = null
        try {
            listLoaded = getGroups(twitter)
        } catch (e: MicroBlogException) {
            DebugLog.w(LOGTAG, tr = e)
        }

        if (listLoaded != null) {
            val listSize = listLoaded.size
            if (listLoaded is PageableResponseList<*>) {
                nextPagination = CursorPagination.valueOf(listLoaded.nextCursor)
                prevPagination = CursorPagination.valueOf(listLoaded.previousCursor)
                val dataSize = data.size
                for (i in 0 until listSize) {
                    val group = listLoaded[i]
                    data.add(ParcelableGroupUtils.from(group, accountKey, dataSize + i, isMember(group)))
                }
            } else {
                for (i in 0 until listSize) {
                    val list = listLoaded[i]
                    data.add(ParcelableGroupUtils.from(listLoaded[i], accountKey, i, isMember(list)))
                }
            }
        }
        Collections.sort(data)
        return data
    }

    public override fun onStartLoading() {
        forceLoad()
    }

    protected open fun isMember(list: Group): Boolean {
        return list.isMember
    }
}
