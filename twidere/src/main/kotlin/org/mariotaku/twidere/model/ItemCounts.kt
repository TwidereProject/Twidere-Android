package org.mariotaku.twidere.model

/**
 * Created by mariotaku on 11/22/16.
 */

class ItemCounts(counts: Int) {
    private val data: IntArray = IntArray(counts)

    fun getItemCountIndex(itemPosition: Int): Int {
        if (itemPosition < 0) return -1
        var sum = 0
        data.forEachIndexed { i, num ->
            sum += num
            if (itemPosition < sum) {
                return i
            }
        }
        return -1
    }

    fun getItemStartPosition(countIndex: Int): Int {
        return (0 until countIndex).sumBy { data[it] }
    }

    val itemCount: Int get() = data.sum()

    val size: Int get() = data.size

    operator fun set(countIndex: Int, value: Int) {
        data[countIndex] = value
    }

    operator fun get(countIndex: Int): Int {
        return data[countIndex]
    }

    fun clear() {
        for (i in data.indices) {
            data[i] = 0
        }
    }

}