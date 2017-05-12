package org.mariotaku.twidere.model

import org.mariotaku.twidere.model.pagination.Pagination
import org.mariotaku.twidere.model.pagination.SinceMaxPagination

/**
 * Created by mariotaku on 16/2/14.
 */
interface RefreshTaskParam {
    val accountKeys: Array<UserKey>

    val pagination: Array<out Pagination?>? get() = null

    val extraId: Long get() = -1

    val isLoadingMore: Boolean get() = false

    val shouldAbort: Boolean get() = false

    val isBackground: Boolean get() = false

    val hasMaxIds: Boolean
        get() = pagination?.any { (it as? SinceMaxPagination)?.maxId != null } ?: false
}
