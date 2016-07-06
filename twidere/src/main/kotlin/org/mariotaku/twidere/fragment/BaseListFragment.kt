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

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_TAB_POSITION
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

open class BaseListFragment : ListFragment(), OnScrollListener, RefreshScrollTopInterface {

    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    var activityFirstCreated: Boolean = false
        private set
    var instanceStateSaved: Boolean = false
        private set
    var reachedBottom: Boolean = false
        private set
    private var notReachedBottomBefore = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponentHelper.build(context).inject(this)
    }

    val application: TwidereApplication
        get() = TwidereApplication.getInstance(activity)

    val contentResolver: ContentResolver?
        get() {
            val activity = activity
            if (activity != null) return activity.contentResolver
            return null
        }

    fun getSharedPreferences(name: String, mode: Int): SharedPreferences? {
        val activity = activity
        if (activity != null) return activity.getSharedPreferences(name, mode)
        return null
    }

    fun getSystemService(name: String): Any? {
        val activity = activity
        if (activity != null) return activity.getSystemService(name)
        return null
    }

    val tabPosition: Int
        get() {
            val args = arguments
            return if (args != null) args.getInt(EXTRA_TAB_POSITION, -1) else -1
        }

    fun invalidateOptionsMenu() {
        val activity = activity ?: return
        activity.invalidateOptionsMenu()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instanceStateSaved = savedInstanceState != null
        val lv = listView
        lv.setOnScrollListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFirstCreated = true
    }

    override fun onDestroy() {
        super.onDestroy()
        activityFirstCreated = true
    }

    fun onPostStart() {
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                          totalItemCount: Int) {
        val reached = firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount >= visibleItemCount

        if (reachedBottom != reached) {
            reachedBottom = reached
            if (reachedBottom && notReachedBottomBefore) {
                notReachedBottomBefore = false
                return
            }
            if (reachedBottom && listAdapter.count > visibleItemCount) {
                onReachedBottom()
            }
        }

    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

    }

    override fun onStart() {
        super.onStart()
        onPostStart()
    }

    override fun onStop() {
        activityFirstCreated = false
        super.onStop()
    }

    override fun scrollToStart(): Boolean {
        Utils.scrollListToTop(listView)
        return true
    }

    override fun setSelection(position: Int) {
        Utils.scrollListToPosition(listView, position)
    }

    override fun triggerRefresh(): Boolean {
        return false
    }

    protected fun onReachedBottom() {

    }
}
