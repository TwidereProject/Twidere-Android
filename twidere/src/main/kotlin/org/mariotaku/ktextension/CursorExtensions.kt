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
            this?.close()
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
            this?.close()
        }
    }
}

val Cursor.isEmpty: Boolean
    get() = count == 0