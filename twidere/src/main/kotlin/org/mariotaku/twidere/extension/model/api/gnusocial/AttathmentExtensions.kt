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

package org.mariotaku.twidere.extension.model.api.gnusocial

import org.mariotaku.microblog.library.gnusocial.model.Attachment
import org.mariotaku.twidere.model.ParcelableMedia

/**
 * Created by mariotaku on 2017/4/30.
 */
fun Attachment.toParcelable(externalUrl: String?) : ParcelableMedia? {
    val mimeType = mimetype ?: return null
    val result = ParcelableMedia()

    when {
        mimeType.startsWith("image/") -> {
            result.type = ParcelableMedia.Type.IMAGE
        }
        mimeType.startsWith("video/") -> {
            result.type = ParcelableMedia.Type.VIDEO
        }
        else -> {
            // https://github.com/TwidereProject/Twidere-Android/issues/729
            // Skip unsupported attachment
            return null
        }
    }
    result.width = width
    result.height = height
    result.url = externalUrl ?: url
    result.page_url = externalUrl ?: url
    result.media_url = url
    result.preview_url = largeThumbUrl
    return result
}