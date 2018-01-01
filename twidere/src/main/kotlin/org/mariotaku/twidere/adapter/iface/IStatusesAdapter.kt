package org.mariotaku.twidere.adapter.iface

import org.mariotaku.twidere.annotation.PreviewStyle
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

interface IStatusesAdapter : IContentAdapter, IGapSupportedAdapter {

    @TwidereLinkify.HighlightStyle
    val linkHighlightingStyle: Int

    val lightFont: Boolean

    @PreviewStyle
    val mediaPreviewStyle: Int

    val twidereLinkify: TwidereLinkify

    val mediaPreviewEnabled: Boolean

    val nameFirst: Boolean

    val sensitiveContentEnabled: Boolean

    val showAccountsColor: Boolean

    val useStarsForLikes: Boolean

    val statusClickListener: IStatusViewHolder.StatusClickListener?

    fun isCardActionsShown(position: Int): Boolean

    fun showCardActions(position: Int)

    fun isFullTextVisible(position: Int): Boolean

    fun setFullTextVisible(position: Int, visible: Boolean)

    /**
     * @param raw Count hidden (filtered) item if `true `
     */
    fun getStatusCount(): Int

    fun getStatus(position: Int): ParcelableStatus

    fun getStatusId(position: Int): String

    fun getStatusTimestamp(position: Int): Long

    fun getStatusPositionKey(position: Int): Long

    fun getAccountKey(position: Int): UserKey

    fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus?

}
