/*
 * Twidere - Twitter client for Android
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
import android.support.v7.widget.RecyclerView.ViewHolder

import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition

/**
 * Created by mariotaku on 15/4/16.
 */
abstract class LoadMoreSupportAdapter<VH : ViewHolder>(context: Context) : BaseRecyclerViewAdapter<VH>(context), ILoadMoreSupportAdapter {

    @IndicatorPosition
    override var loadMoreSupportedPosition: Long = 0
        set(value) {
            field = value
            loadMoreIndicatorPosition = ILoadMoreSupportAdapter.apply(loadMoreIndicatorPosition, value)
            notifyDataSetChanged()
        }

    @IndicatorPosition
    override var loadMoreIndicatorPosition: Long = 0
        set(value) {
            if (field == value) return
            field = ILoadMoreSupportAdapter.apply(value, loadMoreSupportedPosition)
            notifyDataSetChanged()
        }

}
