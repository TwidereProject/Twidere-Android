package org.mariotaku.twidere.extension.model

import org.mariotaku.ktextension.addAllTo
import org.mariotaku.ktextension.isNullOrEmpty
import org.mariotaku.twidere.model.ParcelableActivity
import java.util.*

/**
 * Created by mariotaku on 2016/12/6.
 */
val ParcelableActivity.id: String
    get() = "$min_position-$max_position"

val ParcelableActivity.reachedCountLimit: Boolean get() {
    fun Array<*>?.reachedCountLimit() = if (this == null) false else size > 10

    return sources.reachedCountLimit() || target_statuses.reachedCountLimit() ||
            target_users.reachedCountLimit() || target_user_lists.reachedCountLimit() ||
            target_object_statuses.reachedCountLimit() || target_object_users.reachedCountLimit() ||
            target_object_user_lists.reachedCountLimit()
}

fun ParcelableActivity.isSameSources(another: ParcelableActivity): Boolean {
    return Arrays.equals(sources, another.sources)
}

fun ParcelableActivity.isSameTarget(another: ParcelableActivity): Boolean {
    if (target_statuses.isNullOrEmpty() && target_users.isNullOrEmpty() && target_user_lists
            .isNullOrEmpty()) {
        return false
    }
    return Arrays.equals(target_users, another.target_users) && Arrays.equals(target_statuses,
            another.target_statuses) && Arrays.equals(target_user_lists, another.target_user_lists)
}

fun ParcelableActivity.isSameTargetObject(another: ParcelableActivity): Boolean {
    if (target_object_statuses.isNullOrEmpty() && target_object_users.isNullOrEmpty()
            && target_object_user_lists.isNullOrEmpty()) {
        return false
    }
    return Arrays.equals(target_object_users, another.target_object_users)
            && Arrays.equals(target_object_statuses, another.target_object_statuses)
            && Arrays.equals(target_object_user_lists, another.target_object_user_lists)
}

fun ParcelableActivity.prependSources(another: ParcelableActivity) {
    sources = uniqCombine(another.sources, sources)
}

fun ParcelableActivity.prependTargets(another: ParcelableActivity) {
    target_statuses = uniqCombine(another.target_statuses, target_statuses)
    target_users = uniqCombine(another.target_users, target_users)
    target_user_lists = uniqCombine(another.target_user_lists, target_user_lists)
}

fun ParcelableActivity.prependTargetObjects(another: ParcelableActivity) {
    target_object_statuses = uniqCombine(another.target_object_statuses, target_object_statuses)
    target_object_users = uniqCombine(another.target_object_users, target_object_users)
    target_object_user_lists = uniqCombine(another.target_object_user_lists, target_object_user_lists)
}

private inline fun <reified T> uniqCombine(vararg arrays: Array<T>?): Array<T> {
    val set = mutableSetOf<T>()
    arrays.forEach { array -> array?.addAllTo(set) }
    return set.toTypedArray()
}