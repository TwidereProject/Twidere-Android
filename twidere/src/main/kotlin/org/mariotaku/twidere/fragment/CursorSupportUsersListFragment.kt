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

package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.content.Loader
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.model.ParcelableUser

abstract class CursorSupportUsersListFragment : ParcelableUsersFragment() {

    protected var nextCursor: Long = -1
        private set
    protected var prevCursor: Long = -1
        private set
    protected var nextPage = 1
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            nextCursor = savedInstanceState.getLong(EXTRA_NEXT_CURSOR, -1)
            prevCursor = savedInstanceState.getLong(EXTRA_PREV_CURSOR, -1)
            nextPage = savedInstanceState.getInt(EXTRA_NEXT_PAGE, -1)
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onLoaderReset(loader: Loader<List<ParcelableUser>?>) {
        super.onLoaderReset(loader)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        super.onLoadFinished(loader, data)
        val cursorLoader = loader as CursorSupportUsersLoader
        nextCursor = cursorLoader.nextCursor
        prevCursor = cursorLoader.prevCursor
        nextPage = cursorLoader.nextPage
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L) return
        super.onLoadMoreContents(position.toLong())
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putLong(EXTRA_NEXT_CURSOR, nextCursor)
        loaderArgs.putLong(EXTRA_PAGE, nextPage.toLong())
        loaderManager.restartLoader(0, loaderArgs, this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putLong(EXTRA_NEXT_CURSOR, nextCursor)
        outState.putLong(EXTRA_PREV_CURSOR, prevCursor)
        outState.putLong(EXTRA_NEXT_PAGE, nextPage.toLong())
    }

    abstract override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean): CursorSupportUsersLoader

}
