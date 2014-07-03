/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view.holder;

import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.animation.CardItemAnimation;
import org.mariotaku.twidere.view.iface.ICardItemView;

public class CardViewHolder extends ViewHolder {

	public final CardItemAnimation item_animation;
	public final ICardItemView content;

	public CardViewHolder(final View view) {
		super(view);
		content = (ICardItemView) view.findViewById(R.id.content);
		item_animation = new CardItemAnimation();
	}

}
