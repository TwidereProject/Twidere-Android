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
import android.support.v4.app.Fragment
import android.text.TextUtils
import org.mariotaku.twidere.fragment.card.CardPollFragment
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils

/**
 * Created by mariotaku on 15/1/1.
 */
object TwitterCardUtils {

    private val sFactory = TwitterCardFragmentFactory.instance

    val CARD_NAME_PLAYER = "player"
    val CARD_NAME_AUDIO = "audio"
    val CARD_NAME_ANIMATED_GIF = "animated_gif"

    fun createCardFragment(status: ParcelableStatus): Fragment? {
        val card = status.card
        if (card == null || card.name == null) return null
        if (CARD_NAME_PLAYER == card.name) {
            val playerFragment = sFactory.createPlayerFragment(card)
            if (playerFragment != null) return playerFragment
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
        } else if (CARD_NAME_AUDIO == card.name) {
            val playerFragment = sFactory.createAudioFragment(card)
            if (playerFragment != null) return playerFragment
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
        } else if (CARD_NAME_ANIMATED_GIF == card.name) {
            val playerFragment = sFactory.createAnimatedGifFragment(card)
            if (playerFragment != null) return playerFragment
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
        } else if (CardPollFragment.isPoll(card)) {
            return TwitterCardFragmentFactory.createCardPollFragment(status)
        }
        return null
    }


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
        if (CardPollFragment.isPoll(card)) {
            return true
        }
        return false
    }

    fun isPoll(status: ParcelableStatus): Boolean {
        val card = status.card ?: return false
        return CardPollFragment.isPoll(card)
    }
}
