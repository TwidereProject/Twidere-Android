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
import android.database.Cursor
import android.graphics.Typeface
import android.support.v4.widget.SimpleCursorAdapter
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.mediaPreviewStyleKey
import org.mariotaku.twidere.extension.model.getActionName
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.util.ParcelableMediaUtils
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.holder.DraftViewHolder
import javax.inject.Inject

class DraftsAdapter(context: Context) : SimpleCursorAdapter(context, R.layout.list_item_draft, null, arrayOfNulls<String>(0), IntArray(0), 0) {

    @Inject
    lateinit var imageLoader: MediaLoaderWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    private val mediaLoadingHandler: MediaLoadingHandler
    private val mediaPreviewStyle: Int

    var textSize: Float = 0f
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var indices: DraftCursorIndices? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        mediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)
        mediaPreviewStyle = preferences[mediaPreviewStyleKey]
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val draft = indices?.newObject(cursor) ?: return
        val holder = view.tag as DraftViewHolder
        val accountKeys = draft.account_keys
        val text = draft.text
        val mediaUpdates = draft.media
        val timestamp = draft.timestamp
        val actionType: String = draft.action_type ?: Draft.Action.UPDATE_STATUS
        val actionName = draft.getActionName(context)
        holder.media_preview_container.style = mediaPreviewStyle
        when (actionType) {
            Draft.Action.UPDATE_STATUS, Draft.Action.UPDATE_STATUS_COMPAT_1,
            Draft.Action.UPDATE_STATUS_COMPAT_2, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                val media = ParcelableMediaUtils.fromMediaUpdates(mediaUpdates)
                holder.media_preview_container.visibility = View.VISIBLE
                holder.media_preview_container.displayMedia(loader = imageLoader, media = media,
                        loadingHandler = mediaLoadingHandler)
            }
            else -> {
                holder.media_preview_container.visibility = View.GONE
            }
        }
        if (accountKeys != null) {
            holder.content.drawEnd(*DataStoreUtils.getAccountColors(context, accountKeys))
        } else {
            holder.content.drawEnd()
        }
        holder.setTextSize(textSize)
        val emptyContent = TextUtils.isEmpty(text)
        if (emptyContent) {
            holder.text.setText(R.string.empty_content)
        } else {
            holder.text.text = text
        }
        holder.text.setTypeface(holder.text.typeface, if (emptyContent) Typeface.ITALIC else Typeface.NORMAL)

        if (timestamp > 0) {
            val timeString = Utils.formatSameDayTime(context, timestamp)
            holder.time.text = context.getString(R.string.action_name_saved_at_time, actionName, timeString)
        } else {
            holder.time.text = actionName
        }
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
        val view = super.newView(context, cursor, parent)
        val tag = view.tag
        if (tag !is DraftViewHolder) {
            view.tag = DraftViewHolder(view)
        }
        return view
    }

    override fun swapCursor(c: Cursor?): Cursor? {
        val old = super.swapCursor(c)
        if (c != null) {
            indices = DraftCursorIndices(c)
        }
        return old
    }

    fun getDraft(position: Int): Draft {
        cursor.moveToPosition(position)
        return indices!!.newObject(cursor)
    }
}
