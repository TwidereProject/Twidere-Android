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

fun ParcelableActivity.prependTargets(from: ParcelableActivity) {
    this.targets = (this.targets ?: ParcelableActivity.RelatedObject()).prepend(from.targets)
}

fun ParcelableActivity.prependTargetObjects(from: ParcelableActivity) {
    this.target_objects = (this.target_objects ?: ParcelableActivity.RelatedObject())
            .prepend(from.target_objects)
}

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