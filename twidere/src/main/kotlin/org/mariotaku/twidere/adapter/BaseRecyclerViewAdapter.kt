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
import android.content.SharedPreferences
import androidx.core.text.BidiFormatter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.constant.displayProfileImageKey
import org.mariotaku.twidere.constant.profileImageStyleKey
import org.mariotaku.twidere.constant.showAbsoluteTimeKey
import org.mariotaku.twidere.constant.textSizeKey
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.MultiSelectManager
import org.mariotaku.twidere.util.ReadStateManager
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

/**
 * Created by mariotaku on 15/10/5.
 */
abstract class BaseRecyclerViewAdapter<VH : RecyclerView.ViewHolder>(
        val context: Context,
        override val requestManager: RequestManager
) : RecyclerView.Adapter<VH>(), IContentAdapter {

    @Inject
    final override lateinit var twitterWrapper: AsyncTwitterWrapper

    @Inject
    final override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    final override lateinit var bidiFormatter: BidiFormatter
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var defaultFeatures: DefaultFeatures

    final override val profileImageSize: String = context.getString(R.string.profile_image_size)
    final override val profileImageStyle: Int
    final override val textSize: Float
    final override val profileImageEnabled: Boolean
    final override val showAbsoluteTime: Boolean

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponent.get(context).inject(this as BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)
        profileImageStyle = preferences[profileImageStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        profileImageEnabled = preferences[displayProfileImageKey]
        showAbsoluteTime = preferences[showAbsoluteTimeKey]
    }

}
