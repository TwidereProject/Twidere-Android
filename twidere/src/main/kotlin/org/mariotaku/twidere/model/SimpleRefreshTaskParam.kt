package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 16/2/14.
 */
abstract class SimpleRefreshTaskParam : RefreshTaskParam {

    internal var cached: Array<UserKey>? = null

    override val accountKeys: Array<UserKey>
        get() {
            if (cached != null) return cached!!
            cached = getAccountKeysWorker()
            return cached!!
        }

    abstract fun getAccountKeysWorker(): Array<UserKey>

    override val maxIds: Array<String?>?
        get() = null

    override val sinceIds: Array<String?>?
        get() = null

    override val hasMaxIds: Boolean
        get() = maxIds != null

    override val hasSinceIds: Boolean
        get() = sinceIds != null

    override val sinceSortIds: LongArray?
        get() = null

    override val maxSortIds: LongArray?
        get() = null

    override val isLoadingMore: Boolean
        get() = false

    override val shouldAbort: Boolean
        get() = false
}
