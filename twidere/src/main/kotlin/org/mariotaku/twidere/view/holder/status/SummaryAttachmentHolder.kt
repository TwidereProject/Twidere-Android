package org.mariotaku.twidere.view.holder.status

import android.support.constraint.ConstraintLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.layout_content_item_attachment_summary.view.*
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.model.ParcelableStatus

class SummaryAttachmentHolder(
        parent: StatusViewHolder,
        adapter: IStatusesAdapter,
        view: ConstraintLayout
) : StatusViewHolder.AttachmentHolder(parent, view) {

    private val summaryThumbnail = view.summaryThumbnail
    private val summaryTitle = view.summaryTitle
    private val summaryDescription = view.summaryDescription
    private val summaryDomain = view.summaryDomain

    override fun display(status: ParcelableStatus) {
        val summary = status.attachment!!.summary_card!!

        Glide.with(summaryThumbnail).load(summary.thumbnail).into(summaryThumbnail)

        summaryTitle.text = summary.title
        summaryDescription.text = summary.description
        summaryDomain.text = summary.domain
    }

}
