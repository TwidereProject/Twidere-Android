/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.recyclerview.widget.RecyclerViewAccessor
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.twidere.constant.mediaPreviewStyleKey
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.view.holder.DraftViewHolder
import javax.inject.Inject

class DraftsAdapter(
        context: Context,
        val requestManager: RequestManager
) : SimpleCursorAdapter(context, DraftViewHolder.layoutResource, null, emptyArray(), intArrayOf(), 0) {

    @Inject
    lateinit var preferences: SharedPreferences

    private val mediaPreviewStyle: Int

    var textSize: Float = 0f
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var indices: ObjectCursor.CursorIndices<Draft>? = null

    init {
        GeneralComponent.get(context).inject(this)
        mediaPreviewStyle = preferences[mediaPreviewStyleKey]
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val draft = indices!!.newObject(cursor)

        val holder = view.tag as DraftViewHolder
        RecyclerViewAccessor.setLayoutPosition(holder, cursor.position)

        holder.display(context, requestManager, draft)
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val view = super.newView(context, cursor, parent)
        if (view.tag !is DraftViewHolder) {
            view.tag = DraftViewHolder(view).apply {
                this.setTextSize(textSize)
                this.mediaPreviewContainer.style = mediaPreviewStyle
            }
        }
        return view
    }

    override fun swapCursor(c: Cursor?): Cursor? {
        val old = super.swapCursor(c)
        indices = c?.let { ObjectCursor.indicesFrom(it, Draft::class.java) }
        return old
    }

    fun getDraft(position: Int): Draft {
        cursor.moveToPosition(position)
        return indices!!.newObject(cursor)
    }
}
