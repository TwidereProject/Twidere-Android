package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/14.
 */
abstract class SimpleRefreshTaskParam : RefreshTaskParam {

    internal var cached: Array<UserKey>? = null

    override val maxIds: Array<String?>?
        get() = null

    override val sinceIds: Array<String?>?
        get() = null

    override val cursors: Array<String?>?
        get() = null

    override val sinceSortIds: LongArray?
        get() = null

    override val maxSortIds: LongArray?
        get() = null

    override val extraId: Long
        get() = -1

    override val isLoadingMore: Boolean
        get() = false

    override val shouldAbort: Boolean
        get() = false
}
