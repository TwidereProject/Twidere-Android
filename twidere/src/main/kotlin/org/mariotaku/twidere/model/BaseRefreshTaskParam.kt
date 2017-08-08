package org.mariotaku.twidere.model

import org.mariotaku.twidere.model.pagination.Pagination

/**
 * Created by mariotaku on 16/2/11.
 */
open class BaseRefreshTaskParam(
        override val accountKeys: Array<UserKey>,
        override val pagination: Array<out Pagination?>?
) : RefreshTaskParam {

    override var extraId: Long = -1L
    override var isLoadingMore: Boolean = false
    override var shouldAbort: Boolean = false

}
