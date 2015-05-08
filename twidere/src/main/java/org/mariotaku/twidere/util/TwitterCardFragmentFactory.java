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

import android.support.v4.app.Fragment;

import org.mariotaku.twidere.fragment.support.CardBrowserFragment;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity.ParcelableBindingValue;

import org.mariotaku.twidere.api.twitter.model.CardEntity.BindingValue;

/**
 * Created by mariotaku on 15/1/1.
 */
public abstract class TwitterCardFragmentFactory {

    public abstract Fragment createAnimatedGifFragment(ParcelableCardEntity card);

    public abstract Fragment createAudioFragment(ParcelableCardEntity card);

    public abstract Fragment createPlayerFragment(ParcelableCardEntity card);

    public static TwitterCardFragmentFactory getInstance() {
        return new TwitterCardFragmentFactoryImpl();
    }

    public static Fragment createGenericPlayerFragment(ParcelableCardEntity card) {
        final ParcelableBindingValue player_url = ParcelableCardEntity.getValue(card, "player_url");
        if (player_url == null || !BindingValue.TYPE_STRING.equals(player_url.type)) return null;
        return CardBrowserFragment.show(player_url.value);
    }
}
