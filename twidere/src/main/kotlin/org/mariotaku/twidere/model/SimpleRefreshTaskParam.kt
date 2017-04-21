package org.mariotaku.twidere.model

import org.mariotaku.twidere.model.pagination.Pagination

/**
 * Created by mariotaku on 16/2/14.
 */
abstract class SimpleRefreshTaskParam : RefreshTaskParam {

    override val pagination: Array<out Pagination?>? = null

    override val extraId: Long = -1

    override val isLoadingMore: Boolean = false

    override val shouldAbort: Boolean = false
}
