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

import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity.ParcelableBindingValue;

/**
 * Created by mariotaku on 15/1/1.
 */
public class TwitterCardUtils {

    private static final TwitterCardFragmentFactory sFactory = TwitterCardFragmentFactory.getInstance();

    public static final String CARD_NAME_PLAYER = "player";
    public static final String CARD_NAME_AUDIO = "audio";
    public static final String CARD_NAME_ANIMATED_GIF = "animated_gif";

    @Nullable
    public static Fragment createCardFragment(ParcelableCardEntity card) {
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
        }
        return null;
    }


    public static Point getCardSize(ParcelableCardEntity card) {
        final ParcelableBindingValue player_width = ParcelableCardEntity.getValue(card, "player_width");
        final ParcelableBindingValue player_height = ParcelableCardEntity.getValue(card, "player_height");
        if (player_width != null && player_height != null) {
            final int width = ParseUtils.parseInt(String.valueOf(player_width.value));
            final int height = ParseUtils.parseInt(String.valueOf(player_height.value));
            if (width > 0 && height > 0) {
                return new Point(width, height);
            }
        }
        return null;
    }

    public static boolean isCardSupported(ParcelableCardEntity card) {
        if (card == null || card.name == null) return false;
        switch (card.name) {
            case CARD_NAME_PLAYER: {
                return ParcelableCardEntity.getValue(card, "player_stream_url") == null;
            }
            case CARD_NAME_AUDIO: {
                return true;
            }
        }
        return false;
    }

}
