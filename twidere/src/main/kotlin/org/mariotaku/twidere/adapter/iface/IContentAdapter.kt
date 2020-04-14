/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter.iface

import androidx.core.text.BidiFormatter
import com.bumptech.glide.RequestManager
import org.mariotaku.twidere.annotation.ImageShapeStyle
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.UserColorNameManager

/**
 * Created by mariotaku on 15/1/3.
 */
interface IContentAdapter {

    val userColorNameManager: UserColorNameManager

    fun getItemCount(): Int

    @ImageShapeStyle
    val profileImageStyle: Int

    val profileImageSize: String

    val profileImageEnabled: Boolean

    val textSize: Float

    val twitterWrapper: AsyncTwitterWrapper

    val requestManager: RequestManager

    val bidiFormatter: BidiFormatter

    val showAbsoluteTime: Boolean

}
