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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.OnClickListener
import android.widget.ProgressBar
import android.widget.TextView
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

    private val gapText: TextView
    private val gapProgress: ProgressBar

    init {
        itemView.setOnClickListener(this)
        gapText = itemView.gapText
        gapProgress = itemView.gapProgress
    }

    override fun onClick(v: View) {
        adapter.gapClickListener?.onGapClick(this, layoutPosition)
        display(true)
    }

    fun display(showProgress: Boolean) {
        gapText.visibility = if (showProgress) View.GONE else View.VISIBLE
        gapProgress.visibility = if (showProgress) View.VISIBLE else View.GONE
    }

    companion object {
        const val layoutResource = R.layout.card_item_gap
    }
}
