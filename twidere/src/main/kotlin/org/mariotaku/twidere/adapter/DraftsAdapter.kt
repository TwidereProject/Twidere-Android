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
import android.support.v4.widget.SimpleCursorAdapter
import android.view.View
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.mediaPreviewStyleKey
import org.mariotaku.twidere.extension.model.getActionName
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.draft.StatusObjectExtras
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

    private val mediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)
    private val mediaPreviewStyle: Int

    var textSize: Float = 0f
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var indices: DraftCursorIndices? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        mediaPreviewStyle = preferences[mediaPreviewStyleKey]
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val holder = view.tag as DraftViewHolder
        val draft = indices!!.newObject(cursor)

        val accountKeys = draft.account_keys
        val actionType: String = draft.action_type ?: Draft.Action.UPDATE_STATUS
        val actionName = draft.getActionName(context)
        var summaryText: String? = null
        when (actionType) {
            Draft.Action.SEND_DIRECT_MESSAGE, Draft.Action.SEND_DIRECT_MESSAGE_COMPAT,
            Draft.Action.UPDATE_STATUS, Draft.Action.UPDATE_STATUS_COMPAT_1,
            Draft.Action.UPDATE_STATUS_COMPAT_2, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                val media = ParcelableMediaUtils.fromMediaUpdates(draft.media)
                holder.media_preview_container.visibility = View.VISIBLE
                holder.media_preview_container.displayMedia(loader = imageLoader, media = media,
                        loadingHandler = mediaLoadingHandler)
            }
            Draft.Action.FAVORITE, Draft.Action.RETWEET -> {
                val extras = draft.action_extras as? StatusObjectExtras
                if (extras != null) {
                    summaryText = extras.status.text_unescaped
                }
                holder.media_preview_container.visibility = View.GONE
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
        if (summaryText != null) {
            holder.text.text = summaryText
        } else if (draft.text.isNullOrEmpty()) {
            holder.text.setText(R.string.empty_content)
        } else {
            holder.text.text = draft.text
        }

        if (draft.timestamp > 0) {
            val timeString = Utils.formatSameDayTime(context, draft.timestamp)
            holder.time.text = context.getString(R.string.action_name_saved_at_time, actionName, timeString)
        } else {
            holder.time.text = actionName
        }
    }

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup): View {
        val view = super.newView(context, cursor, parent)
        if (view.tag !is DraftViewHolder) {
            view.tag = DraftViewHolder(view).apply {
                this.media_preview_container.style = mediaPreviewStyle
            }
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
