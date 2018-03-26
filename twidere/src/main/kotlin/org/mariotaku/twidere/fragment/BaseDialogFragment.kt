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

import android.content.Context
import android.content.SharedPreferences
import android.support.v4.app.DialogFragment
import nl.komponents.kovenant.Promise
import okhttp3.Dns
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.fragment.iface.IBaseFragment
import org.mariotaku.twidere.util.DebugModeUtils
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.sync.DataSyncProvider
import javax.inject.Inject

open class BaseDialogFragment : DialogFragment(), IBaseFragment<BaseDialogFragment> {

    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var keyboardShortcutsHandler: KeyboardShortcutsHandler
    @Inject
    lateinit var dns: Dns
    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var dataSyncProvider: DataSyncProvider

    private val actionHelper = IBaseFragment.ActionHelper<BaseDialogFragment>()

    override fun onResume() {
        super.onResume()
        actionHelper.dispatchOnResumeFragments(this)
    }

    override fun onPause() {
        actionHelper.dispatchOnPause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        DebugModeUtils.watchReferenceLeak(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        GeneralComponent.get(context!!).inject(this)
    }

    override fun executeAfterFragmentResumed(useHandler: Boolean, action: (BaseDialogFragment) -> Unit)
            : Promise<Unit, Exception> {
        return actionHelper.executeAfterFragmentResumed(this, useHandler, action)
    }


}
