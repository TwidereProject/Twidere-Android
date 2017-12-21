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

package org.mariotaku.twidere.extension.api

import org.mariotaku.ktextension.subArray
import org.mariotaku.microblog.library.Mastodon
import org.mariotaku.microblog.library.model.mastodon.Relationship


fun Mastodon.batchGetRelationships(ids: Collection<String>): Map<String, Relationship> {
    val list = ids.toList()
    val indices = ids.indices
    val result = HashMap<String, Relationship>()
    @Suppress("LoopToCallChain")
    for (i in indices step 100) {
        val batch = list.subArray(i until (i + 100).coerceAtMost(indices.last))
        if (batch.isEmpty()) continue
        getRelationships(batch).forEach {
            result[it.id] = it
        }
    }
    return result
}