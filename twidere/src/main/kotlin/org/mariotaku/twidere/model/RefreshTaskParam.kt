package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/14.
 */
interface RefreshTaskParam {
    val accountKeys: Array<UserKey>

    val maxIds: Array<String?>?

    val sinceIds: Array<String?>?

    val maxSortIds: LongArray?

    val sinceSortIds: LongArray?

    val hasMaxIds: Boolean

    val hasSinceIds: Boolean

    val isLoadingMore: Boolean

    val shouldAbort: Boolean

}
