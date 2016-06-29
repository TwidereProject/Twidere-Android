package org.mariotaku.twidere.adapter.iface

import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.MediaLoadingHandler
import org.mariotaku.twidere.util.TwidereLinkify
import org.mariotaku.twidere.view.CardMediaContainer
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/18.
 */
interface IStatusesAdapter<Data> : IContentCardAdapter, IGapSupportedAdapter {

    @TwidereLinkify.HighlightStyle
    val linkHighlightingStyle: Int

    @CardMediaContainer.PreviewStyle
    val mediaPreviewStyle: Int

    val statusCount: Int

    val rawStatusCount: Int

    val twidereLinkify: TwidereLinkify

    val mediaPreviewEnabled: Boolean

    val nameFirst: Boolean

    val sensitiveContentEnabled: Boolean

    val showAccountsColor: Boolean

    val useStarsForLikes: Boolean

    val mediaLoadingHandler: MediaLoadingHandler

    val statusClickListener: IStatusViewHolder.StatusClickListener?

    fun isCardActionsShown(position: Int): Boolean

    fun showCardActions(position: Int)

    fun setData(data: Data?): Boolean

    fun getStatus(position: Int): ParcelableStatus?

    fun getStatusId(position: Int): String?

    fun getStatusTimestamp(position: Int): Long

    fun getStatusPositionKey(position: Int): Long

    fun getAccountKey(position: Int): UserKey?

    fun findStatusById(accountKey: UserKey, statusId: String): ParcelableStatus?

}
