package org.mariotaku.ktextension

import android.util.SparseBooleanArray

/**
 * Created by mariotaku on 2016/12/27.
 */
operator fun SparseBooleanArray.set(key: Int, value: Boolean) {
    put(key, value)
}