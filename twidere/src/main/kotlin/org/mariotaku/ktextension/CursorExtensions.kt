package org.mariotaku.ktextension

import android.database.Cursor
import org.mariotaku.library.objectcursor.ObjectCursor
import java.util.*

fun <T> Cursor.map(indices: ObjectCursor.CursorIndices<T>): List<T> {
    val list = ArrayList<T>()
    moveToFirst()
    while (!isAfterLast) {
        list.add(indices.newObject(this))
        moveToNext()
    }
    return list
}

inline val Cursor.isEmpty: Boolean
    get() = count == 0


/**
 * @param limit -1 for no limit
 * @return Remaining count, -1 if no rows present
 */
inline fun Cursor.forEachRow(limit: Int = -1, action: (cur: Cursor, pos: Int) -> Boolean): Int {
    moveToFirst()
    var current = 0
    while (!isAfterLast) {
        @Suppress("ConvertTwoComparisonsToRangeCheck")
        if (limit >= 0 && current >= limit) break
        if (action(this, current)) {
            current++
        }
        moveToNext()
    }
    return count - position
}
