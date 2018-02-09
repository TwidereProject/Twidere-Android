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

package org.mariotaku.twidere.fragment.drafts

import android.os.Bundle
import org.mariotaku.ktextension.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACTIONS
import org.mariotaku.twidere.extension.title
import org.mariotaku.twidere.fragment.AbsToolbarTabPagesFragment
import org.mariotaku.twidere.model.Draft

/**
 * Created by mariotaku on 2017/10/4.
 */

class DraftsFragment : AbsToolbarTabPagesFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        title = getString(R.string.title_drafts)
    }

    override fun addTabs(adapter: SupportTabsAdapter) {
        adapter.add(DraftsListFragment::class.java, Bundle {
            putStringArray(EXTRA_ACTIONS, null)
        }, getString(R.string.label_drafts_all))
        adapter.add(DraftsListFragment::class.java, Bundle {
            putStringArray(EXTRA_ACTIONS, arrayOf(Draft.Action.UPDATE_STATUS,
                    Draft.Action.UPDATE_STATUS_COMPAT_1, Draft.Action.UPDATE_STATUS_COMPAT_2,
                    Draft.Action.QUOTE, Draft.Action.REPLY))
        }, getString(R.string.label_drafts_statuses))
        adapter.add(DraftsListFragment::class.java, Bundle {
            putStringArray(EXTRA_ACTIONS, arrayOf(Draft.Action.SEND_DIRECT_MESSAGE,
                    Draft.Action.SEND_DIRECT_MESSAGE_COMPAT, Draft.Action.FAVORITE,
                    Draft.Action.RETWEET))
        }, getString(R.string.label_drafts_others))
    }

}
