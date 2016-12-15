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

import android.os.Bundle
import android.support.v4.app.Fragment

import org.mariotaku.twidere.fragment.card.CardBrowserFragment
import org.mariotaku.twidere.fragment.card.CardPollFragment
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils
import java.util.*

/**
 * Created by mariotaku on 15/1/1.
 */
abstract class TwitterCardFragmentFactory {

    abstract fun createAnimatedGifFragment(card: ParcelableCardEntity): Fragment?

    abstract fun createAudioFragment(card: ParcelableCardEntity): Fragment?

    abstract fun createPlayerFragment(card: ParcelableCardEntity): Fragment?

    companion object {

        val instance: TwitterCardFragmentFactory by lazy {
            ServiceLoader.load(TwitterCardFragmentFactory::class.java).first()
        }

        fun createGenericPlayerFragment(card: ParcelableCardEntity?, args: Bundle?): Fragment? {
            if (card == null) return null
            val playerUrl = ParcelableCardEntityUtils.getString(card, "player_url") ?: return null
            return CardBrowserFragment.show(playerUrl, args)
        }

        fun createCardPollFragment(status: ParcelableStatus): Fragment {
            return CardPollFragment.show(status)
        }

        fun createCardFragment(status: ParcelableStatus): Fragment? {
            val card = status.card
            if (card == null || card.name == null) return null
            if (TwitterCardUtils.CARD_NAME_PLAYER == card.name) {
                val playerFragment = instance.createPlayerFragment(card)
                if (playerFragment != null) return playerFragment
                return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
            } else if (TwitterCardUtils.CARD_NAME_AUDIO == card.name) {
                val playerFragment = instance.createAudioFragment(card)
                if (playerFragment != null) return playerFragment
                return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
            } else if (TwitterCardUtils.CARD_NAME_ANIMATED_GIF == card.name) {
                val playerFragment = instance.createAnimatedGifFragment(card)
                if (playerFragment != null) return playerFragment
                return TwitterCardFragmentFactory.createGenericPlayerFragment(card, null)
            } else if (TwitterCardUtils.isPoll(card)) {
                return TwitterCardFragmentFactory.createCardPollFragment(status)
            }
            return null
        }

    }
}
