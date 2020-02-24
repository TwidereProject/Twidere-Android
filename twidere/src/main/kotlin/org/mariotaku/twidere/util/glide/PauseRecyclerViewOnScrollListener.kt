/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.glide

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager

class PauseRecyclerViewOnScrollListener(
        private val pauseOnScroll: Boolean,
        private val pauseOnFling: Boolean,
        private val requestManager: RequestManager
) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                if (!requestManager.isPaused) return
                requestManager.resumeRequests()
            }
            RecyclerView.SCROLL_STATE_DRAGGING -> if (this.pauseOnScroll) {
                if (requestManager.isPaused) return
                requestManager.pauseRequests()
            }
            RecyclerView.SCROLL_STATE_SETTLING -> if (this.pauseOnFling) {
                if (requestManager.isPaused) return
                requestManager.pauseRequests()
            }
        }
    }

}
