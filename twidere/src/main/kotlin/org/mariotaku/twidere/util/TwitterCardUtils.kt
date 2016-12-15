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

package org.mariotaku.twidere.util

import android.graphics.Point
import android.text.TextUtils
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils
import java.util.regex.Pattern

/**
 * Created by mariotaku on 15/1/1.
 */
object TwitterCardUtils {

    val PATTERN_POLL_TEXT_ONLY: Pattern = Pattern.compile("poll([\\d]+)choice_text_only")

    const val CARD_NAME_PLAYER = "player"
    const val CARD_NAME_AUDIO = "audio"
    const val CARD_NAME_ANIMATED_GIF = "animated_gif"

    fun getCardSize(card: ParcelableCardEntity): Point? {
        val playerWidth = ParcelableCardEntityUtils.getAsInteger(card, "player_width", -1)
        val playerHeight = ParcelableCardEntityUtils.getAsInteger(card, "player_height", -1)
        if (playerWidth > 0 && playerHeight > 0) {
            return Point(playerWidth, playerHeight)
        }
        return null
    }

    fun isCardSupported(status: ParcelableStatus): Boolean {
        val card = status.card ?: return false
        when (status.card_name) {
            CARD_NAME_PLAYER -> {
                status.media?.let { mediaArray ->
                    val appUrlResolved = ParcelableCardEntityUtils.getString(card, "app_url_resolved")
                    val cardUrl = card.url
                    mediaArray.forEach {
                        if (it.url == appUrlResolved || it.url == cardUrl) {
                            return false
                        }
                    }
                }
                return TextUtils.isEmpty(ParcelableCardEntityUtils.getString(card, "player_stream_url"))
            }
            CARD_NAME_AUDIO -> {
                return true
            }
        }
        if (isPoll(card)) {
            return true
        }
        return false
    }

    fun getChoicesCount(card: ParcelableCardEntity): Int {
        val matcher = PATTERN_POLL_TEXT_ONLY.matcher(card.name)
        if (!matcher.matches()) throw IllegalStateException()
        return NumberUtils.toInt(matcher.group(1))
    }

    fun isPoll(card: ParcelableCardEntity): Boolean {
        return PATTERN_POLL_TEXT_ONLY.matcher(card.name).matches() && !TextUtils.isEmpty(card.url)
    }

    fun isPoll(status: ParcelableStatus): Boolean {
        val card = status.card ?: return false
        return isPoll(card)
    }
}
