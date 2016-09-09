package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/11.
 */
class BaseRefreshTaskParam(
        override val accountKeys: Array<UserKey>,
        override val maxIds: Array<String?>?,
        override val sinceIds: Array<String?>?,
        override val maxSortIds: LongArray? = null,
        override val sinceSortIds: LongArray? = null
) : RefreshTaskParam {
    override var isLoadingMore: Boolean = false
    override var shouldAbort: Boolean = false

    override val hasMaxIds: Boolean
        get() = maxIds != null

    override val hasSinceIds: Boolean
        get() = sinceIds != null
}
