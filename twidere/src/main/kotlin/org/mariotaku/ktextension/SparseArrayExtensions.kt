package org.mariotaku.ktextension

import androidx.collection.LongSparseArray
import android.util.SparseBooleanArray

/**
 * Created by mariotaku on 2016/12/27.
 */
operator fun SparseBooleanArray.set(key: Int, value: Boolean) {
    put(key, value)
}

operator fun <E> LongSparseArray<E>.set(key: Long, value: E) {
    put(key, value)
}