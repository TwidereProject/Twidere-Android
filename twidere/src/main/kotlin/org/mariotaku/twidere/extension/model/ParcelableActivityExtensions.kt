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

package org.mariotaku.twidere.extension.model

import org.mariotaku.ktextension.addAllTo
import org.mariotaku.microblog.library.model.microblog.Activity.Action
import org.mariotaku.twidere.model.ParcelableActivity
import java.util.*

val ParcelableActivity.activityStatus: ParcelableActivity?
    get() = takeIf { action in Action.MENTION_ACTIONS }

val ParcelableActivity.reachedCountLimit: Boolean
    get() {
        return sources.reachedCountLimit() || targets.reachedCountLimit() ||
                target_objects.reachedCountLimit()
    }

fun ParcelableActivity.isSameSources(another: ParcelableActivity): Boolean {
    return Arrays.equals(sources, another.sources)
}

fun ParcelableActivity.isSameTarget(another: ParcelableActivity): Boolean {
    if (targets.isNullOrEmpty()) {
        return false
    }
    return targets == another.targets
}

fun ParcelableActivity.isSameTargetObject(another: ParcelableActivity): Boolean {
    if (targets.isNullOrEmpty()) {
        return false
    }
    return target_objects == another.target_objects
}

fun ParcelableActivity.prependSources(another: ParcelableActivity) {
    sources = uniqCombine(another.sources, sources)
}

fun ParcelableActivity.prependTargets(from: ParcelableActivity) {
    this.targets = (this.targets ?: ParcelableActivity.RelatedObject()).prepend(from.targets)
}

fun ParcelableActivity.prependTargetObjects(from: ParcelableActivity) {
    this.target_objects = (this.target_objects ?: ParcelableActivity.RelatedObject())
            .prepend(from.target_objects)
}

fun ParcelableActivity.updateActivityFilterInfo() {
    updateFilterInfo(sources?.singleOrNull()?.let {
        listOf(it.description_unescaped, it.location, it.url_expanded)
    })
}

inline val ParcelableActivity.RelatedObject.size
    get() = when {
        statuses != null -> statuses.size
        users != null -> users.size
        user_lists != null -> user_lists.size
        else -> 0
    }

fun ParcelableActivity.RelatedObject?.isNullOrEmpty(): Boolean {
    if (this == null) return true
    return size == 0
}

private inline fun <reified T> uniqCombine(vararg arrays: Array<T>?): Array<T> {
    val set = mutableSetOf<T>()
    arrays.forEach { array -> array?.addAllTo(set) }
    return set.toTypedArray()
}

private fun Array<*>?.reachedCountLimit() = if (this == null) false else size > 10

private fun ParcelableActivity.RelatedObject?.reachedCountLimit() = if (this == null) false else size > 10

private fun ParcelableActivity.RelatedObject.prepend(from: ParcelableActivity.RelatedObject?): ParcelableActivity.RelatedObject {
    this.statuses = uniqCombine(this.statuses, from?.statuses)
    this.users = uniqCombine(this.users, from?.users)
    this.user_lists = uniqCombine(this.user_lists, from?.user_lists)
    return this
}