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

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import org.mariotaku.twidere.Constants.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.activity.iface.IControlBarActivity.ControlBarOffsetListener
import org.mariotaku.twidere.adapter.SupportTabsAdapter
import org.mariotaku.twidere.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.RecentSearchProvider
import org.mariotaku.twidere.provider.TwidereDataStore.SearchHistory

class SearchFragment : AbsToolbarTabPagesFragment(), RefreshScrollTopInterface, SupportFragmentCallback, SystemWindowsInsetsCallback, ControlBarOffsetListener, OnPageChangeListener, LinkHandlerActivity.HideUiOnScroll {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState == null && !TextUtils.isEmpty(query)) {
            val suggestions = SearchRecentSuggestions(activity,
                    RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE)
            suggestions.saveRecentQuery(query, null)
            val values = ContentValues()
            values.put(SearchHistory.QUERY, query)
            contentResolver.insert(SearchHistory.CONTENT_URI, values)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_search, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (isDetached || activity == null) return
        val item = menu!!.findItem(R.id.compose)
        item.title = getString(R.string.tweet_hashtag, query)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.save -> {
                val twitter = twitterWrapper
                val args = arguments
                if (args != null) {
                    twitter.createSavedSearchAsync(accountKey, query)
                }
                return true
            }
            R.id.compose -> {
                val intent = Intent(activity, ComposeActivity::class.java)
                intent.action = INTENT_ACTION_COMPOSE
                if (query.startsWith("@") || query.startsWith("\uff20")) {
                    intent.putExtra(Intent.EXTRA_TEXT, query)
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, String.format("#%s ", query))
                }
                intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun triggerRefresh(position: Int): Boolean {
        return false
    }

    val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!!

    val query: String
        get() = arguments.getString(EXTRA_QUERY)!!


    override fun addTabs(adapter: SupportTabsAdapter) {
        adapter.addTab(StatusesSearchFragment::class.java, arguments, getString(R.string.statuses), R.drawable.ic_action_twitter, 0, null)
        adapter.addTab(SearchUsersFragment::class.java, arguments, getString(R.string.users), R.drawable.ic_action_user, 1, null)
    }


}
