package org.mariotaku.ktextension

import android.database.Cursor
import android.database.sqlite.SQLiteBlobTooBigException
import org.mariotaku.library.objectcursor.ObjectCursor
import java.util.*

/**
 * Created by mariotaku on 16/6/29.
 */

fun Cursor.safeMoveToPosition(pos: Int) = try {
    moveToPosition(pos)
} catch (e: IllegalStateException) {
    false
} catch (e: SQLiteBlobTooBigException) {
    false
}

fun Cursor.safeGetLong(columnIndex: Int, def: Long = -1) = try {
    getLong(columnIndex)
} catch (e: IllegalStateException) {
    def
}

fun Cursor.safeGetInt(columnIndex: Int, def: Int = -1) = try {
    getInt(columnIndex)
} catch (e: IllegalStateException) {
    def
}

fun Cursor.safeGetString(columnIndex: Int, def: String = ""): String = try {
    getString(columnIndex)
} catch (e: IllegalStateException) {
    def
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

val Cursor.isEmpty: Boolean
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
