package org.mariotaku.twidere.model

import android.database.Cursor
import org.mariotaku.twidere.provider.TwidereDataStore
import org.mariotaku.twidere.provider.TwidereDataStore.*

class SuggestionItem(cursor: Cursor, indices: Indices) {

    val title: String?
    val summary: String?
    val _id: Long
    val extra_id: String?

    init {
        _id = cursor.getLong(indices._id)
        title = cursor.getString(indices.title)
        summary = cursor.getString(indices.summary)
        extra_id = cursor.getString(indices.extra_id)
    }

    class Indices(cursor: Cursor) {
        val _id: Int = cursor.getColumnIndex(Suggestions._ID)
        val type: Int = cursor.getColumnIndex(Suggestions.TYPE)
        val title: Int = cursor.getColumnIndex(Suggestions.TITLE)
        val value: Int = cursor.getColumnIndex(Suggestions.VALUE)
        val summary: Int = cursor.getColumnIndex(Suggestions.SUMMARY)
        val icon: Int = cursor.getColumnIndex(Suggestions.ICON)
        val extra_id: Int = cursor.getColumnIndex(Suggestions.EXTRA_ID)

    }
}