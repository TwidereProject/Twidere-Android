/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.net.Uri
import androidx.cursoradapter.widget.SimpleCursorAdapter
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.spannable
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.displayProfileImageKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.extension.loadProfileImage
import org.mariotaku.twidere.model.ParcelableLiteUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.view.ProfileImageView
import javax.inject.Inject


class UserAutoCompleteAdapter(
        val context: Context,
        val requestManager: RequestManager
) : SimpleCursorAdapter(context, R.layout.list_item_auto_complete, null, emptyArray(), intArrayOf(), 0) {

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    private val displayProfileImage: Boolean
    private var profileImageStyle: Int

    private var indices: ObjectCursor.CursorIndices<ParcelableLiteUser>? = null

    var accountKey: UserKey? = null

    init {
        GeneralComponent.get(context).inject(this)
        displayProfileImage = preferences[displayProfileImageKey]
        profileImageStyle = preferences[profileImageStyleKey]
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val user = indices!!.newObject(cursor)
        val text1 = view.findViewById<TextView>(android.R.id.text1)
        val text2 = view.findViewById<TextView>(android.R.id.text2)
        val icon = view.findViewById<ProfileImageView>(android.R.id.icon)

        icon.style = profileImageStyle

        text1.spannable = userColorNameManager.getUserNickname(user.key, user.name)
        text2.spannable = "@${user.screen_name}"
        if (displayProfileImage) {
            requestManager.loadProfileImage(context, user, profileImageStyle).into(icon)
        } else {
            //TODO cancel image load
        }

        icon.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
    }

    fun closeCursor() {
        val cursor = swapCursor(null) ?: return
        if (!cursor.isClosed) {
            cursor.close()
        }
    }

    override fun convertToString(cursor: Cursor): CharSequence {
        return cursor.getString(indices!![CachedUsers.SCREEN_NAME])
    }

    override fun runQueryOnBackgroundThread(constraint: CharSequence): Cursor? {
        if (TextUtils.isEmpty(constraint)) return null
        val filter = filterQueryProvider
        if (filter != null) return filter.runQuery(constraint)
        val query = constraint.toString()
        val queryEscaped = query.replace("_", "^_")
        val nicknameKeys = Utils.getMatchedNicknameKeys(query, userColorNameManager)
        val usersSelection = Expression.or(
                Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
                Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size))
        val selectionArgs = arrayOf(queryEscaped, queryEscaped, *nicknameKeys)
        val order = arrayOf(CachedUsers.LAST_SEEN, CachedUsers.SCORE, CachedUsers.SCREEN_NAME, CachedUsers.NAME)
        val ascending = booleanArrayOf(false, false, true, true)
        val orderBy = OrderBy(order, ascending)
        val uri = Uri.withAppendedPath(CachedUsers.CONTENT_URI_WITH_SCORE, accountKey.toString())
        return context.contentResolver.query(uri, CachedUsers.COLUMNS, usersSelection.sql,
                selectionArgs, orderBy.sql)
    }

    override fun swapCursor(cursor: Cursor?): Cursor? {
        indices = cursor?.let { ObjectCursor.indicesFrom(it, ParcelableLiteUser::class.java) }
        return super.swapCursor(cursor)
    }

}
