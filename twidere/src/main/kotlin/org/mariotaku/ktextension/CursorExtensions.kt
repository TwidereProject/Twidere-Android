package org.mariotaku.ktextension

import android.database.Cursor

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