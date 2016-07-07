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
import android.database.Cursor
import android.net.Uri
import android.support.v4.widget.SimpleCursorAdapter
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject


class UserAutoCompleteAdapter(context: Context) : SimpleCursorAdapter(context, R.layout.list_item_auto_complete, null, UserAutoCompleteAdapter.FROM, UserAutoCompleteAdapter.TO, 0) {

    @Inject
    lateinit var profileImageLoader: MediaLoaderWrapper
    @Inject
    lateinit var mPreferences: SharedPreferencesWrapper
    @Inject
    lateinit var mUserColorNameManager: UserColorNameManager

    private val mDisplayProfileImage: Boolean

    private var mIdIdx: Int = 0
    private var mNameIdx: Int = 0
    private var mScreenNameIdx: Int = 0
    private var mProfileImageIdx: Int = 0
    private var mAccountKey: UserKey? = null

    init {
        GeneralComponentHelper.build(context).inject(this)
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)
    }

    override fun bindView(view: View, context: Context?, cursor: Cursor) {
        val text1 = view.findViewById(android.R.id.text1) as TextView
        val text2 = view.findViewById(android.R.id.text2) as TextView
        val icon = view.findViewById(android.R.id.icon) as ImageView

        // Clear images in order to prevent images in recycled view shown.
        icon.setImageDrawable(null)

        text1.text = mUserColorNameManager.getUserNickname(cursor.getString(mIdIdx), cursor.getString(mNameIdx))
        text2.text = String.format("@%s", cursor.getString(mScreenNameIdx))
        if (mDisplayProfileImage) {
            val profileImageUrl = cursor.getString(mProfileImageIdx)
            profileImageLoader.displayProfileImage(icon, profileImageUrl)
        } else {
            profileImageLoader.cancelDisplayTask(icon)
        }

        icon.visibility = if (mDisplayProfileImage) View.VISIBLE else View.GONE
        super.bindView(view, context, cursor)
    }

    fun closeCursor() {
        val cursor = swapCursor(null) ?: return
        if (!cursor.isClosed) {
            cursor.close()
        }
    }

    override fun convertToString(cursor: Cursor?): CharSequence {
        return cursor!!.getString(mScreenNameIdx)
    }

    override fun runQueryOnBackgroundThread(constraint: CharSequence): Cursor? {
        if (TextUtils.isEmpty(constraint)) return null
        val filter = filterQueryProvider
        if (filter != null) return filter.runQuery(constraint)
        val query = constraint.toString()
        val queryEscaped = query.replace("_", "^_")
        val nicknameKeys = Utils.getMatchedNicknameKeys(query, mUserColorNameManager)
        val usersSelection = Expression.or(
                Expression.likeRaw(Columns.Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                Expression.likeRaw(Columns.Column(CachedUsers.NAME), "?||'%'", "^"),
                Expression.inArgs(Columns.Column(CachedUsers.USER_KEY), nicknameKeys.size))
        val selectionArgs = arrayOf(queryEscaped, queryEscaped, *nicknameKeys)
        val order = arrayOf(CachedUsers.LAST_SEEN, CachedUsers.SCORE, CachedUsers.SCREEN_NAME, CachedUsers.NAME)
        val ascending = booleanArrayOf(false, false, true, true)
        val orderBy = OrderBy(order, ascending)
        val uri = Uri.withAppendedPath(CachedUsers.CONTENT_URI_WITH_SCORE, mAccountKey.toString())
        return mContext.contentResolver.query(uri, CachedUsers.COLUMNS, usersSelection.sql,
                selectionArgs, orderBy.sql)
    }


    fun setAccountKey(accountKey: UserKey) {
        mAccountKey = accountKey
    }

    override fun swapCursor(cursor: Cursor?): Cursor? {
        if (cursor != null) {
            mIdIdx = cursor.getColumnIndex(CachedUsers.USER_KEY)
            mNameIdx = cursor.getColumnIndex(CachedUsers.NAME)
            mScreenNameIdx = cursor.getColumnIndex(CachedUsers.SCREEN_NAME)
            mProfileImageIdx = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL)
        }
        return super.swapCursor(cursor)
    }

    companion object {

        private val FROM = arrayOfNulls<String>(0)
        private val TO = IntArray(0)
    }


}
