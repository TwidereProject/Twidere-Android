/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view.holder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.View.OnClickListener
import kotlinx.android.synthetic.main.card_item_gap.view.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IGapSupportedAdapter

/**
 * Created by mariotaku on 14/12/3.
 */
class GapViewHolder(
        private val adapter: IGapSupportedAdapter,
        itemView: View
) : RecyclerView.ViewHolder(itemView), OnClickListener {

    private val gapText = itemView.gapText
    private val gapProgress = itemView.gapProgress

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        adapter.gapClickListener?.onGapClick(this, layoutPosition)
        display(true)
    }

    fun display(showProgress: Boolean) {
        if (showProgress) {
            gapText.visibility = View.INVISIBLE
            gapProgress.visibility = View.VISIBLE
            gapProgress.spin()
        } else {
            gapText.visibility = View.VISIBLE
            gapProgress.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val layoutResource = R.layout.card_item_gap
    }
}
