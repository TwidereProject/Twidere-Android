package org.mariotaku.twidere.adapter.iface;

import org.mariotaku.twidere.model.ItemCounts

/**
 * Created by mariotaku on 16/8/19.
 */
interface IItemCountsAdapter {

    val itemCounts: ItemCounts

    fun getItemCountIndex(position: Int): Int {
        return itemCounts.getItemCountIndex(position)
    }

    fun getItemStartPosition(index: Int): Int {
        return itemCounts.getItemStartPosition(index)
    }

}