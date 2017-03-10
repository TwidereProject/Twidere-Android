package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/14.
 */
interface RefreshTaskParam {
    val accountKeys: Array<UserKey>

    val maxIds: Array<String?>?

    val sinceIds: Array<String?>?

    val cursors: Array<String?>?

    val maxSortIds: LongArray?

    val sinceSortIds: LongArray?

    val hasMaxIds: Boolean
        get() = maxIds != null

    val hasSinceIds: Boolean
        get() = sinceIds != null

    val hasCursors: Boolean
        get() = cursors != null

    val extraId: Long

    val isLoadingMore: Boolean

    val shouldAbort: Boolean

}
