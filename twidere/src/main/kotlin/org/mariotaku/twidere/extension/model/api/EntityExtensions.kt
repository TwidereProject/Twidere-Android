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

package org.mariotaku.twidere.extension.model.api

import org.mariotaku.microblog.library.model.microblog.*
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableMediaUtils.getTypeInt
import org.mariotaku.twidere.util.media.preview.PreviewMediaExtractor.fromLink

inline val MediaEntity.bestMediaUrl: String?
    get() = mediaUrlHttps ?: mediaUrl

fun UserMentionEntity.toParcelable(host: String?): ParcelableUserMention {
    val obj = ParcelableUserMention()
    obj.key = UserKey(id, host)
    obj.name = name
    obj.screen_name = screenName
    return obj
}

fun EntitySupport.getEntityMedia(): Array<ParcelableMedia> {
    val list = ArrayList<ParcelableMedia>()
    val mediaEntities = if (this is ExtendedEntitySupport) {
        extendedMediaEntities ?: this.mediaEntities
    } else {
        this.mediaEntities
    }
    mediaEntities?.mapNotNullTo(list) { media ->
        return@mapNotNullTo media.toParcelable()
    }
    urlEntities?.mapNotNullTo(list) {
        fromLink(it.expandedUrl)
    }
    return list.toTypedArray()
}

fun MediaEntity.toParcelable(): ParcelableMedia {
    val media = ParcelableMedia()
    val mediaUrl = bestMediaUrl
    media.url = mediaUrl.orEmpty()
    media.media_url = mediaUrl
    media.preview_url = mediaUrl
    media.page_url = expandedUrl
    media.type = getTypeInt(type)
    media.alt_text = altText
    val size = sizes[MediaEntity.ScaleType.LARGE]
    if (size != null) {
        media.width = size.width
        media.height = size.height
    } else {
        media.width = 0
        media.height = 0
    }
    media.video_info = ParcelableMedia.VideoInfo.fromMediaEntityInfo(videoInfo)
    return media
}

fun UrlEntity.getStartEndForEntity(out: IntArray): Boolean {
    out[0] = start
    out[1] = end
    return true
}