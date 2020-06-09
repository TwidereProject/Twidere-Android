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

package org.mariotaku.twidere.util.twitter.card

import org.mariotaku.twidere.extension.model.getString
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.TwitterCardUtils
import org.mariotaku.twidere.view.ContainerView
import org.mariotaku.twidere.view.controller.twitter.card.CardBrowserViewController
import org.mariotaku.twidere.view.controller.twitter.card.CardPollViewController
import java.util.*

/**
 * Created by mariotaku on 15/1/1.
 */
abstract class TwitterCardViewFactory {

    abstract fun from(status: ParcelableStatus): ContainerView.ViewController?

    companion object {
        fun from(status: ParcelableStatus): ContainerView.ViewController? {
            val vc = fromImplementations(status)
            if (vc != null) return vc
            return createCardFragment(status)
        }

        private fun fromImplementations(status: ParcelableStatus): ContainerView.ViewController? {
            ServiceLoader.load(TwitterCardViewFactory::class.java).forEach { factory ->
                val vc = factory.from(status)
                if (vc != null) return vc
            }
            return null
        }

        private fun createCardFragment(status: ParcelableStatus): ContainerView.ViewController? {
            val card = status.card
            if (card?.name == null) return null
            return when {
                TwitterCardUtils.CARD_NAME_PLAYER == card.name -> {
                    createGenericPlayerFragment(card)
                }
                TwitterCardUtils.CARD_NAME_AUDIO == card.name -> {
                    createGenericPlayerFragment(card)
                }
                TwitterCardUtils.isPoll(card) -> {
                    createCardPollFragment(status)
                }
                else -> null
            }
        }


        private fun createCardPollFragment(status: ParcelableStatus): ContainerView.ViewController {
            return CardPollViewController.show(status)
        }

        private fun createGenericPlayerFragment(card: ParcelableCardEntity): ContainerView.ViewController? {
            val playerUrl = card.getString("player_url") ?: return null
            return CardBrowserViewController.show(playerUrl)
        }
    }
}
