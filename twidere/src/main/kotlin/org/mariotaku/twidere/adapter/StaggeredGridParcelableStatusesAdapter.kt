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
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.R
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder

/**
 * Created by mariotaku on 14/11/19.
 */
class StaggeredGridParcelableStatusesAdapter(
        context: Context,
        requestManager: RequestManager
) : ParcelableStatusesAdapter(context, requestManager) {

    override fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder {
        val view = inflater.inflate(R.layout.adapter_item_media_status, parent, false)
        val holder = MediaStatusViewHolder(this, view)
        holder.setOnClickListeners()
        holder.setupViewOptions()
        return holder
    }

}
