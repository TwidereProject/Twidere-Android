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
import android.graphics.PorterDuff.Mode
import android.support.v4.widget.SimpleCursorAdapter
import android.view.View
import android.widget.TextView
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.constant.displayProfileImageKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.model.SuggestionItem
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Suggestions
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.view.ProfileImageView
import javax.inject.Inject

class ComposeAutoCompleteAdapter(context: Context) : SimpleCursorAdapter(context,
        R.layout.list_item_auto_complete, null, emptyArray(), intArrayOf(), 0) {

    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    private val displayProfileImage: Boolean
    private val profileImageStyle: Int

    private var indices: SuggestionItem.Indices? = null
    var accountKey: UserKey? = null
    private var token: Char = ' '

    init {
        GeneralComponentHelper.build(context).inject(this)
        displayProfileImage = preferences[displayProfileImageKey]
        profileImageStyle = preferences[profileImageStyleKey]
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val indices = this.indices!!
        val text1 = view.findViewById(android.R.id.text1) as TextView
        val text2 = view.findViewById(android.R.id.text2) as TextView
        val icon = view.findViewById(android.R.id.icon) as ProfileImageView

        icon.style = profileImageStyle

        if (Suggestions.AutoComplete.TYPE_USERS == cursor.getString(indices.type)) {
            text1.text = userColorNameManager.getUserNickname(cursor.getString(indices.extra_id),
                    cursor.getString(indices.title))
            text2.text = String.format("@%s", cursor.getString(indices.summary))
            if (displayProfileImage) {
                val profileImageUrl = cursor.getString(indices.icon)
                mediaLoader.displayProfileImage(icon, profileImageUrl)
            } else {
                //TODO cancel image load
            }

            icon.clearColorFilter()
        } else {
            text1.text = String.format("#%s", cursor.getString(indices.title))
            text2.setText(R.string.hashtag)

            icon.setImageResource(R.drawable.ic_action_hashtag)
            icon.setColorFilter(text1.currentTextColor, Mode.SRC_ATOP)
        }
        icon.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
        super.bindView(view, context, cursor)
    }

    fun closeCursor() {
        val cursor = swapCursor(null) ?: return
        if (!cursor.isClosed) {
            cursor.close()
        }
    }

    override fun convertToString(cursor: Cursor): CharSequence {
        val indices = this.indices!!
        when (cursor.getString(indices.type)) {
            Suggestions.AutoComplete.TYPE_HASHTAGS -> {
                return '#' + cursor.getString(indices.value)
            }
            Suggestions.AutoComplete.TYPE_USERS -> {
                return '@' + cursor.getString(indices.value)
            }
        }
        return cursor.getString(indices.value)
    }

    override fun runQueryOnBackgroundThread(constraint: CharSequence?): Cursor? {
        if (constraint == null || constraint.isEmpty()) return null
        val token = constraint[0]
        if (getNormalizedSymbol(token) == getNormalizedSymbol(this.token)) {
            val filter = filterQueryProvider
            if (filter != null) return filter.runQuery(constraint)
        }
        this.token = token
        val builder = Suggestions.AutoComplete.CONTENT_URI.buildUpon()
        builder.appendQueryParameter(QUERY_PARAM_QUERY, constraint.subSequence(1, constraint.length).toString())
        when (getNormalizedSymbol(token)) {
            '#' -> {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, Suggestions.AutoComplete.TYPE_HASHTAGS)
            }
            '@' -> {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, Suggestions.AutoComplete.TYPE_USERS)
            }
            else -> {
                return null
            }
        }
        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        return mContext.contentResolver.query(builder.build(), Suggestions.AutoComplete.COLUMNS,
                null, null, null)
    }

    override fun swapCursor(cursor: Cursor?): Cursor? {
        if (cursor != null) {
            indices = SuggestionItem.Indices(cursor)
        }
        return super.swapCursor(cursor)
    }

    companion object {

        private fun getNormalizedSymbol(character: Char): Char {
            when (character) {
                '\uff20', '@' -> return '@'
                '\uff03', '#' -> return '#'
            }
            return '\u0000'
        }
    }

}
