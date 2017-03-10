package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/11.
 */
open class BaseRefreshTaskParam(
        override val accountKeys: Array<UserKey>,
        override val maxIds: Array<String?>?,
        override val sinceIds: Array<String?>?,
        override val cursors: Array<String?>? = null,
        override val maxSortIds: LongArray? = null,
        override val sinceSortIds: LongArray? = null
) : RefreshTaskParam {
    override var extraId: Long = -1L
    override var isLoadingMore: Boolean = false
    override var shouldAbort: Boolean = false

}
