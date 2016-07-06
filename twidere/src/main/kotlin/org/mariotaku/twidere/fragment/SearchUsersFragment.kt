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
import org.mariotaku.twidere.annotation.Referral
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.UserSearchLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey

class SearchUsersFragment : ParcelableUsersFragment() {

    private var page = 1

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            page = savedInstanceState.getInt(EXTRA_PAGE, 1)
        }
        super.onActivityCreated(savedInstanceState)
    }

    public override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableUser>?> {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val query = args.getString(EXTRA_QUERY)
        val page = args.getInt(EXTRA_PAGE, 1)
        return UserSearchLoader(context, accountKey, query, page, adapter!!.getData(), fromUser)
    }

    override fun onLoadFinished(loader: Loader<List<ParcelableUser>?>, data: List<ParcelableUser>?) {
        super.onLoadFinished(loader, data)
        if (loader is UserSearchLoader) {
            page = loader.page
        }
    }

    override fun onLoadMoreContents(@IndicatorPosition position: Long) {
        // Only supports load from end, skip START flag
        if (position and ILoadMoreSupportAdapter.START !== 0L) return
        super.onLoadMoreContents(position.toLong())
        if (position == 0L) return
        val loaderArgs = Bundle(arguments)
        loaderArgs.putBoolean(EXTRA_FROM_USER, true)
        loaderArgs.putInt(EXTRA_PAGE, page + 1)
        loaderManager.restartLoader<List<ParcelableUser>>(0, loaderArgs, this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState!!.putInt(EXTRA_PAGE, page)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        page = 1
        super.onDestroyView()
    }

    override val userReferral: String? = Referral.SEARCH_RESULT
}
