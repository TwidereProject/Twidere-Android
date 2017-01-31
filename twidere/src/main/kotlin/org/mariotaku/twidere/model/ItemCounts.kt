package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 11/22/16.
 */

class ItemCounts(counts: Int) {
    private val data: IntArray = IntArray(counts)

    fun getItemCountIndex(itemPosition: Int): Int {
        var sum = 0
        for (i in data.indices) {
            sum += data[i]
            if (itemPosition < sum) {
                return i
            }
        }
        return -1
    }

    fun getItemStartPosition(countIndex: Int): Int {
        var sum = 0
        for (i in 0..countIndex - 1) {
            sum += data[i]
        }
        return sum
    }

    val itemCount: Int get() = data.sum()

    val size: Int get() = data.size

    operator fun set(countIndex: Int, value: Int) {
        data[countIndex] = value
    }

}