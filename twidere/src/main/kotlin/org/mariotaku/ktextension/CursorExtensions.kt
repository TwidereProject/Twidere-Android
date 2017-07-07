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

fun Cursor.safeGetLong(columnIndex: Int, def: Long = -1): Long {
    try {
        return getLong(columnIndex)
    } catch(e: IllegalStateException) {
        return def
    }
}

fun Cursor.safeGetInt(columnIndex: Int, def: Int = -1): Int {
    try {
        return getInt(columnIndex)
    } catch(e: IllegalStateException) {
        return def
    }
}

fun Cursor.safeGetString(columnIndex: Int, def: String = ""): String {
    try {
        return getString(columnIndex)
    } catch(e: IllegalStateException) {
        return def
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

/**
 * Executes the given [block] function on this resource and then closes it down correctly whether an exception
 * is thrown or not.
 *
 * @param block a function to process this closable resource.
 * @return the result of [block] function on this closable resource.
 */
inline fun <R> Cursor.useCursor(block: (Cursor) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this.close()
        } catch (closeException: Exception) {
            // eat the closeException as we are already throwing the original cause
            // and we don't want to mask the real exception

            // TODO on Java 7 we should call
            // e.addSuppressed(closeException)
            // to work like try-with-resources
            // http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html#suppressed-exceptions
        }
        throw e
    } finally {
        if (!closed) {
            this.close()
        }
    }
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
