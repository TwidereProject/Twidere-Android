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

package org.mariotaku.twidere.loader

import android.content.Context

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.statusnet.model.Group
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.UserKey

class UserGroupsLoader(
        context: Context,
        accountKey: UserKey,
        private val userKey: UserKey?,
        private val screenName: String?,
        data: List<ParcelableGroup>?
) : BaseGroupsLoader(context, accountKey, 0, data) {

    @Throws(MicroBlogException::class)
    override fun getGroups(twitter: MicroBlog): ResponseList<Group> {
        if (userKey != null) {
            return twitter.getGroups(userKey.id)
        } else if (screenName != null) {
            return twitter.getGroups(screenName)
        }
        throw MicroBlogException("No user argument")
    }

    override fun isMember(list: Group): Boolean {
        return true
    }
}
