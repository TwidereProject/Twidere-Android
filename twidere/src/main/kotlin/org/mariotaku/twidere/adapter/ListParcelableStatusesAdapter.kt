package org.mariotaku.twidere.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter
import org.mariotaku.twidere.view.holder.StatusViewHolder
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/19.
 */
class ListParcelableStatusesAdapter(
        context: Context,
        getRequestManager: () -> RequestManager
) : ParcelableStatusesAdapter(context, getRequestManager) {

    override val progressViewIds: IntArray
        get() = intArrayOf(R.id.media_preview_progress)

    override fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder {
        return createStatusViewHolder(this, inflater, parent)
    }

    companion object {

        fun createStatusViewHolder(adapter: IStatusesAdapter<*>,
                inflater: LayoutInflater, parent: ViewGroup): StatusViewHolder {
            val view = inflater.inflate(StatusViewHolder.layoutResource, parent, false)
            val holder = StatusViewHolder(adapter, view)
            holder.setOnClickListeners()
            holder.setupViewOptions()
            return holder
        }
    }
}
