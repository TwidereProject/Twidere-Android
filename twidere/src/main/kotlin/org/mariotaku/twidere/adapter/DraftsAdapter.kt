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
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_MEDIA_PREVIEW_STYLE
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.DraftCursorIndices
import org.mariotaku.twidere.model.ParcelableMediaUpdate
import org.mariotaku.twidere.model.UserKey
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

    private var mTextSize: Float = 0.toFloat()
    private var mIndices: DraftCursorIndices? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        mediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)
        mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null))
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val holder = view.tag as DraftViewHolder
        val indices = mIndices!!
        val accountKeys = UserKey.arrayOf(cursor.getString(indices.account_keys))
        val text = cursor.getString(indices.text)
        val mediaUpdates = JsonSerializer.parseArray(cursor.getString(indices.media), ParcelableMediaUpdate::class.java)
        val timestamp = cursor.getLong(indices.timestamp)
        val actionType: String = cursor.getString(indices.action_type) ?: Draft.Action.UPDATE_STATUS
        val actionName = getActionName(context, actionType)
        holder.media_preview_container.setStyle(mediaPreviewStyle)
        when (actionType) {
            Draft.Action.UPDATE_STATUS, Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2, Draft.Action.REPLY, Draft.Action.QUOTE -> {
                val media = ParcelableMediaUtils.fromMediaUpdates(mediaUpdates)
                holder.media_preview_container.visibility = View.VISIBLE
                holder.media_preview_container.displayMedia(media, imageLoader, null, -1, null,
                        mediaLoadingHandler)
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
        holder.setTextSize(mTextSize)
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

    fun setTextSize(text_size: Float) {
        mTextSize = text_size
    }

    override fun swapCursor(c: Cursor?): Cursor? {
        val old = super.swapCursor(c)
        if (c != null) {
            mIndices = DraftCursorIndices(c)
        }
        return old
    }

    private fun getActionName(context: Context, actionType: String): String? {
        if (TextUtils.isEmpty(actionType)) return context.getString(R.string.update_status)
        when (actionType) {
            Draft.Action.UPDATE_STATUS, Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2 -> {
                return context.getString(R.string.update_status)
            }
            Draft.Action.REPLY -> {
                return context.getString(R.string.reply)
            }
            Draft.Action.QUOTE -> {
                return context.getString(R.string.quote)
            }
            Draft.Action.SEND_DIRECT_MESSAGE, Draft.Action.SEND_DIRECT_MESSAGE_COMPAT -> {
                return context.getString(R.string.send_direct_message)
            }
        }
        return null
    }
}
