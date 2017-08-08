package org.mariotaku.ktextension

import android.support.v7.widget.RecyclerView

/**
 * Created by mariotaku on 16/8/21.
 */
fun RecyclerView.Adapter<*>.findPositionByItemId(itemId: Long): Int {
    return (0 until itemCount).firstOrNull { getItemId(it) == itemId } ?: RecyclerView.NO_POSITION
}
