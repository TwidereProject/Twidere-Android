/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension.model.api.mastodon

import org.mariotaku.microblog.library.mastodon.model.Attachment
import org.mariotaku.twidere.model.ParcelableMedia

/**
 * Extension functions for `Attachment` class
 * Created by mariotaku on 2017/4/19.
 */

/**
 * @return [ParcelableMedia] converted media for Twidere
 */
fun Attachment.toParcelable(): ParcelableMedia {
    val result = ParcelableMedia()
    result.type = when (type) {
        "image" -> ParcelableMedia.Type.IMAGE
        "video" -> ParcelableMedia.Type.VIDEO
        "gifv" -> ParcelableMedia.Type.ANIMATED_GIF
        else -> ParcelableMedia.Type.UNKNOWN
    }
    result.height = meta?.original?.height?.toInt() ?: 0
    result.width = meta?.original?.width?.toInt() ?: 0
    result.url = url ?: remoteUrl
    result.media_url = result.url
    result.preview_url = previewUrl
    result.page_url = textUrl
    return result
}