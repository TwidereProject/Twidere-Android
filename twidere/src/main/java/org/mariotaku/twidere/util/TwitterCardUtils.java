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

package org.mariotaku.twidere.util;

import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.fragment.support.card.CardPollFragment;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;

/**
 * Created by mariotaku on 15/1/1.
 */
public class TwitterCardUtils {

    private static final TwitterCardFragmentFactory sFactory = TwitterCardFragmentFactory.getInstance();

    public static final String CARD_NAME_PLAYER = "player";
    public static final String CARD_NAME_AUDIO = "audio";
    public static final String CARD_NAME_ANIMATED_GIF = "animated_gif";

    @Nullable
    public static Fragment createCardFragment(ParcelableStatus status) {
        final ParcelableCardEntity card = status.card;
        if (card == null || card.name == null) return null;
        if (CARD_NAME_PLAYER.equals(card.name)) {
            final Fragment playerFragment = sFactory.createPlayerFragment(card);
            if (playerFragment != null) return playerFragment;
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card);
        } else if (CARD_NAME_AUDIO.equals(card.name)) {
            final Fragment playerFragment = sFactory.createAudioFragment(card);
            if (playerFragment != null) return playerFragment;
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card);
        } else if (CARD_NAME_ANIMATED_GIF.equals(card.name)) {
            final Fragment playerFragment = sFactory.createAnimatedGifFragment(card);
            if (playerFragment != null) return playerFragment;
            return TwitterCardFragmentFactory.createGenericPlayerFragment(card);
        } else if (CardPollFragment.isPoll(card.name)) {
            return TwitterCardFragmentFactory.createCardPollFragment(status);
        }
        return null;
    }


    public static Point getCardSize(ParcelableCardEntity card) {
        final int playerWidth = card.getAsInteger("player_width", -1);
        final int playerHeight = card.getAsInteger("player_height", -1);
        if (playerWidth > 0 && playerHeight > 0) {
            return new Point(playerWidth, playerHeight);
        }
        return null;
    }

    public static boolean isCardSupported(ParcelableStatus status) {
        if (status.card == null || status.card.name == null) return false;
        switch (status.card.name) {
            case CARD_NAME_PLAYER: {
                return ParcelableCardEntity.getValue(status.card, "player_stream_url") == null;
            }
            case CARD_NAME_AUDIO: {
                return true;
            }
        }
        if (CardPollFragment.isPoll(status.card.name)) {
            return true;
        }
        return false;
    }

}
