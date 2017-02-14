package org.mariotaku.ktextension

import android.os.Bundle
import android.os.Parcelable

inline fun Bundle(action: Bundle.() -> Unit): Bundle {
    val bundle = Bundle()
    action(bundle)
    return bundle
}

operator fun Bundle.set(key: String, value: Boolean) {
    return putBoolean(key, value)
}

operator fun Bundle.set(key: String, value: Int) {
    return putInt(key, value)
}

operator fun Bundle.set(key: String, value: Long) {
    return putLong(key, value)
}

operator fun Bundle.set(key: String, value: String?) {
    return putString(key, value)
}

operator fun Bundle.set(key: String, value: Parcelable?) {
    return putParcelable(key, value)
}

operator fun Bundle.set(key: String, value: Array<out Parcelable>?) {
    return putParcelableArray(key, value)
}

operator fun Bundle.set(key: String, value: Array<String>?) {
    return putStringArray(key, value)
}

fun <T> Bundle.getTypedArray(key: String, creator: Parcelable.Creator<T>): Array<T> {
    return getParcelableArray(key).toTypedArray(creator)
}