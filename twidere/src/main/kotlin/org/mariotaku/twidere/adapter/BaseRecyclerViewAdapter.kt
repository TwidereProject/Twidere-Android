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
import android.support.v4.text.BidiFormatter
import android.support.v7.widget.RecyclerView
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.constant.displayProfileImageKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.constant.showAbsoluteTimeKey
import org.mariotaku.twidere.constant.textSizeKey
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

/**
 * Created by mariotaku on 15/10/5.
 */
abstract class BaseRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(
        val context: Context
) : RecyclerView.Adapter<VH>(), IContentAdapter {
    @Inject
    override final lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    override final lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    override final lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    override final lateinit var bidiFormatter: BidiFormatter

    override final val profileImageStyle: Int
    override final val textSize: Float
    override final val profileImageEnabled: Boolean
    override final val showAbsoluteTime: Boolean

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)
        profileImageStyle = preferences[profileImageStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        profileImageEnabled = preferences[displayProfileImageKey]
        showAbsoluteTime = preferences[showAbsoluteTimeKey]
    }

}
