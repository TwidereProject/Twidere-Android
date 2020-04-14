package org.mariotaku.twidere.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.RequestManager
import java.util.*

/**
 * Created by mariotaku on 14/10/27.
 */
abstract class ArrayRecyclerAdapter<T, H : ViewHolder>(
        context: Context,
        requestManager: RequestManager
) : BaseRecyclerViewAdapter<H>(context, requestManager) {

    protected val data = ArrayList<T>()

    override fun onBindViewHolder(holder: H, position: Int) {
        onBindViewHolder(holder, position, getItem(position))
    }

    abstract fun onBindViewHolder(holder: H, position: Int, item: T)


    fun add(item: T?) {
        if (item == null) return
        data.add(item)
        notifyDataSetChanged()
    }

    fun addAll(collection: Collection<T>) {
        data.addAll(collection)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): T {
        return data[position]
    }

    fun remove(position: Int): Boolean {
        data.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    fun removeAll(collection: List<T>) {
        data.removeAll(collection)
        notifyDataSetChanged()
    }

    fun sort(comparator: Comparator<in T>) {
        Collections.sort(data, comparator)
        notifyDataSetChanged()
    }
}
