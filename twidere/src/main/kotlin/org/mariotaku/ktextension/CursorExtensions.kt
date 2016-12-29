package org.mariotaku.ktextension

import android.database.Cursor
import org.mariotaku.library.objectcursor.ObjectCursor
import java.util.*

/**
 * Created by mariotaku on 16/6/29.
 */

fun Cursor.safeMoveToPosition(pos: Int): Boolean {
    try {
        return moveToPosition(pos)
    } catch(e: IllegalStateException) {
        return false
    }
}

fun <T> Cursor.map(indices: ObjectCursor.CursorIndices<T>): List<T> {
    val list = ArrayList<T>()
    moveToFirst()
    while (!isAfterLast) {
        list.add(indices.newObject(this))
        moveToNext()
    }
    return list
}