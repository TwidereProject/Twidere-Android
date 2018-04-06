package org.mariotaku.twidere.adapter

import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import java.util.*

abstract class RecyclerPagerAdapter<VH : RecyclerPagerAdapter.ViewHolder> : PagerAdapter() {
    private val viewHolders: SparseArray<VH> = SparseArray()
    private val recyclerPool: SparseArray<Recycler> = SparseArray()

    final override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var holder = viewHolders.get(position)
        if (holder == null) {
            val itemViewType = getItemViewType(position)
            holder = obtainRecycler(itemViewType).pop() ?: onCreateViewHolder(container, position, itemViewType)
            holder.position = position
            holder.itemViewType = itemViewType
            viewHolders.put(position, holder)

            container.addView(holder.itemView)
            holder.onAttach()
        }
        onBindViewHolder(holder, position, holder.itemViewType)
        return holder
    }

    @Suppress("UNCHECKED_CAST")
    final override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val holder = obj as VH
        holder.position = -1
        holder.onDetach()
        viewHolders.remove(position)
        obtainRecycler(holder.itemViewType).push(holder)
        container.removeView(holder.itemView)
    }

    final override fun getItemPosition(obj: Any): Int {
        for (i in 0 until viewHolders.size()) {
            val position = viewHolders.keyAt(i)
            if (position >= count) continue
            val holder = viewHolders.valueAt(i)
            if (holder === obj) {
                onBindViewHolder(holder, position, holder.itemViewType)
                return POSITION_UNCHANGED
            }
        }
        return POSITION_NONE
    }

    final override fun isViewFromObject(view: View, obj: Any): Boolean {
        return (obj as ViewHolder).itemView === view
    }

    abstract fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): VH

    abstract fun onBindViewHolder(holder: VH, position: Int, itemViewType: Int)

    open fun getItemViewType(position: Int): Int = 0

    fun notifyPagesChanged(invalidateCache: Boolean = true) {
        if (invalidateCache) {
            viewHolders.clear()
        }
        super.notifyDataSetChanged()
    }

    abstract class ViewHolder(val itemView: View) {
        var position: Int = NO_POSITION

        var itemViewType: Int = 0

        open fun onDetach() {

        }

        open fun onAttach() {

        }

        open fun setPrimaryItem(primary: Boolean) {

        }
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        for (i in 0 until viewHolders.size()) {
            val holderPosition = viewHolders.keyAt(i)
            val holder = viewHolders.valueAt(i)
            holder.setPrimaryItem(position == holderPosition)
        }
    }

    private fun obtainRecycler(viewType: Int): Recycler {
        val recycler = recyclerPool.get(viewType)
        if (recycler != null) return recycler
        val newRecycler = Recycler()
        recyclerPool.put(viewType, newRecycler)
        return newRecycler
    }

    private inner class Recycler {
        private val items: Stack<VH> = Stack()

        fun pop(): VH? {
            if (items.empty()) return null
            return items.pop()
        }

        fun push(vh: VH) {
            items.push(vh)
        }
    }

    companion object {
        const val NO_POSITION = -1
    }
}