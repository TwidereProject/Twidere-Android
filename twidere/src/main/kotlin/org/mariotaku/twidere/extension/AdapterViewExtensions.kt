package org.mariotaku.twidere.extension

import android.widget.AdapterView

/**
 * Created by mariotaku on 2017/1/25.
 */
fun AdapterView<*>.setSelectedItem(obj: Any?) {
    for (i in 0 until count) {
        if (adapter.getItem(i) == obj) {
            setSelection(i)
            return
        }
    }
}

