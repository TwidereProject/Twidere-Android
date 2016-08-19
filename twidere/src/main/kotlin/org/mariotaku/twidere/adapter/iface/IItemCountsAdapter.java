package org.mariotaku.twidere.adapter.iface;

/**
 * Created by mariotaku on 16/8/19.
 */
interface IItemCountsAdapter {

    val itemCounts: IntArray

    fun getItemCountIndex(position: Int): Int {
        var sum: Int = 0
        itemCounts.forEachIndexed { idx, count ->
            sum += count
            if (position < sum) {
                return idx
            }
        }
        return -1
    }

    fun getItemStartPosition(index: Int): Int {
        return itemCounts.slice(0 until index).sum()
    }

}