package org.mariotaku.ktextension

import android.os.Parcelable

fun <T> Array<Parcelable>.toTypedArray(creator: Parcelable.Creator<T>): Array<T> {
    val result = creator.newArray(size)
    System.arraycopy(this, 0, result, 0, size)
    return result
}
