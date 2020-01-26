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

package org.mariotaku.twidere.adapter

import android.content.Context
import android.content.SharedPreferences
import androidx.core.text.BidiFormatter
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.adapter.iface.IItemCountsAdapter
import org.mariotaku.twidere.adapter.iface.ILoadMoreSupportAdapter
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.model.ItemCounts
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

open class BaseArrayAdapter<T>(
        context: Context,
        layoutRes: Int,
        collection: Collection<T>? = null,
        override val requestManager: RequestManager
) : ArrayAdapter<T>(context, layoutRes, collection), IContentAdapter, ILoadMoreSupportAdapter,
        IItemCountsAdapter {
    val linkify: TwidereLinkify

    @Inject
    override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    override lateinit var bidiFormatter: BidiFormatter
    @Inject
    override lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var permissionsManager: PermissionsManager

    final override val profileImageSize: String = context.getString(R.string.profile_image_size)
    final override val profileImageStyle: Int
    final override val textSize: Float
    final override val profileImageEnabled: Boolean
    final override val showAbsoluteTime: Boolean
    val nameFirst: Boolean

    override val itemCounts: ItemCounts = ItemCounts(1)


    override var loadMoreSupportedPosition: Long = 0
        @ILoadMoreSupportAdapter.IndicatorPosition set(value) {
            field = value
            loadMoreIndicatorPosition = ILoadMoreSupportAdapter.apply(loadMoreIndicatorPosition, value)
            notifyDataSetChanged()
        }

    override var loadMoreIndicatorPosition: Long = 0
        @ILoadMoreSupportAdapter.IndicatorPosition set(value) {
            if (field == value) return
            field = ILoadMoreSupportAdapter.apply(value, loadMoreSupportedPosition)
            notifyDataSetChanged()
        }

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponent.get(context).inject(this as BaseArrayAdapter<Any>)
        linkify = TwidereLinkify(OnLinkClickHandler(context, multiSelectManager, preferences))
        profileImageStyle = preferences[profileImageStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        profileImageEnabled = preferences[displayProfileImageKey]
        showAbsoluteTime = preferences[showAbsoluteTimeKey]
        nameFirst = preferences[nameFirstKey]
    }

    override fun getItemCount(): Int = count

}
