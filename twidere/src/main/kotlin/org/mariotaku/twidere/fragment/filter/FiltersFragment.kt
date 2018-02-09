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

package org.mariotaku.twidere.fragment.filter

import android.os.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.extension.title
import org.mariotaku.twidere.fragment.AbsToolbarTabPagesFragment

class FiltersFragment : AbsToolbarTabPagesFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
        title = getString(R.string.title_filters)
    }

    override fun addTabs(adapter: SupportTabsAdapter) {
        adapter.add(cls = FilteredUsersFragment::class.java, name = getString(R.string.filter_type_users), tag = "users")
        adapter.add(cls = FilteredKeywordsFragment::class.java, name = getString(R.string.filter_type_keywords), tag = "keywords")
        adapter.add(cls = FilteredSourcesFragment::class.java, name = getString(R.string.filter_type_sources), tag = "sources")
        adapter.add(cls = FilteredLinksFragment::class.java, name = getString(R.string.filter_type_links), tag = "links")
        adapter.add(cls = FilterSettingsFragment::class.java, name = getString(R.string.action_settings), tag = "settings")
    }

}

