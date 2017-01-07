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
import android.support.v4.text.BidiFormatter
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.adapter.iface.IContentAdapter
import org.mariotaku.twidere.constant.*
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

open class BaseArrayAdapter<T>(
        context: Context,
        layoutRes: Int,
        collection: Collection<T>? = null
) : ArrayAdapter<T>(context, layoutRes, collection), IContentAdapter {
    val linkify: TwidereLinkify

    @Inject
    override lateinit var userColorNameManager: UserColorNameManager
    @Inject
    override lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    override lateinit var bidiFormatter: BidiFormatter
    @Inject
    override lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var multiSelectManager: MultiSelectManager
    @Inject
    lateinit var preferences: SharedPreferencesWrapper

    final override val profileImageStyle: Int
    final override val textSize: Float
    final override val profileImageEnabled: Boolean
    final override val isShowAbsoluteTime: Boolean
    val nameFirst: Boolean

    init {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as BaseArrayAdapter<Any>)
        linkify = TwidereLinkify(OnLinkClickHandler(context, multiSelectManager, preferences))
        profileImageStyle = preferences[profileImageStyleKey]
        textSize = preferences[textSizeKey].toFloat()
        profileImageEnabled = preferences[displayProfileImageKey]
        isShowAbsoluteTime = preferences[showAbsoluteTimeKey]
        nameFirst = preferences[nameFirstKey]
    }

    override fun getItemCount(): Int = count


}
