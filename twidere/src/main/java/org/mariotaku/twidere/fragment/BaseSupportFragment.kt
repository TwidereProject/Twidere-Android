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
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.text.BidiFormatter
import com.squareup.otto.Bus
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

open class BaseSupportFragment : Fragment(), IBaseFragment, Constants {

    // Utility classes
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var asyncTaskManager: AsyncTaskManager
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var bidiFormatter: BidiFormatter
    @Inject
    lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    lateinit var validator: TwidereValidator

    private val actionHelper = IBaseFragment.ActionHelper(this)

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        requestFitSystemWindows()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        GeneralComponentHelper.build(context!!).inject(this)
    }

    val contentResolver: ContentResolver
        get() {
            return activity!!.contentResolver!!
        }

    fun invalidateOptionsMenu() {
        val activity = activity ?: return
        activity.supportInvalidateOptionsMenu()
    }

    override fun getExtraConfiguration(): Bundle? {
        return null
    }

    override fun getTabPosition(): Int {
        val args = arguments
        return if (args != null) args.getInt(IntentConstants.EXTRA_TAB_POSITION, -1) else -1
    }

    override fun requestFitSystemWindows() {
        val activity = activity
        val parentFragment = parentFragment
        val callback: IBaseFragment.SystemWindowsInsetsCallback
        if (parentFragment is IBaseFragment.SystemWindowsInsetsCallback) {
            callback = parentFragment
        } else if (activity is IBaseFragment.SystemWindowsInsetsCallback) {
            callback = activity
        } else {
            return
        }
        val insets = Rect()
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets)
        }
    }

    override fun executeAfterFragmentResumed(action: IBaseFragment.Action) {
        actionHelper.executeAfterFragmentResumed(action)
    }

    override fun onResume() {
        super.onResume()
        actionHelper.dispatchOnResumeFragments()
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugModeUtils.watchReferenceLeak(this)
    }

    protected open fun fitSystemWindows(insets: Rect) {
        val view = view
        view?.setPadding(insets.left, insets.top, insets.right, insets.bottom)
    }
}
