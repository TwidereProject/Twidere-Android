package org.mariotaku.twidere.model.util

import org.mariotaku.ktextension.addAllTo
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.model.ParcelableActivity
import java.util.*

val ParcelableActivity.activityStatus: ParcelableActivity?
    get() = when (action) {
        Activity.Action.MENTION, Activity.Action.REPLY, Activity.Action.QUOTE -> this
        else -> null
    }

val ParcelableActivity.id2: String
    get() = "$min_position-$max_position"

val ParcelableActivity.reachedCountLimit: Boolean get() {
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

fun ParcelableActivity.prependTargets(another: ParcelableActivity) {
}

fun ParcelableActivity.prependTargetObjects(another: ParcelableActivity) {
}

private inline fun <reified T> uniqCombine(vararg arrays: Array<T>?): Array<T> {
    val set = mutableSetOf<T>()
    arrays.forEach { array -> array?.addAllTo(set) }
    return set.toTypedArray()
}


private fun Array<*>?.reachedCountLimit() = if (this == null) false else size > 10
private fun List<*>?.reachedCountLimit() = if (this == null) false else size > 10
private fun ParcelableActivity.RelatedObject?.reachedCountLimit() = if (this == null) false else size > 10

inline val ParcelableActivity.RelatedObject.size get() = when {
    statuses != null -> statuses.size
    users != null -> users.size
    user_lists != null -> user_lists.size
    else -> 0
}

fun ParcelableActivity.RelatedObject?.isNullOrEmpty(): Boolean {
    if (this == null) return true
    return size == 0
}