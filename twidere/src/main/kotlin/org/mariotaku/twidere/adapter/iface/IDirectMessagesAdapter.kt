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

package org.mariotaku.twidere.adapter.iface

import org.mariotaku.twidere.model.ParcelableDirectMessage
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.view.CardMediaContainer
import org.mariotaku.twidere.view.ShapedImageView

interface IDirectMessagesAdapter {

    val mediaLoader: MediaLoaderWrapper

    val profileImageEnabled: Boolean

    val textSize: Float

    @ShapedImageView.ShapeStyle
    val profileImageStyle: Int

    @CardMediaContainer.PreviewStyle
    val mediaPreviewStyle: Int

    fun findItem(id: Long): ParcelableDirectMessage?
}
