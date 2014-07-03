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

import android.content.Context;
import android.view.View;

import org.mariotaku.twidere.Constants;

public class ViewHolder implements Constants {

	public View view;

	public ViewHolder(final View view) {
		if (view == null) throw new NullPointerException();
		this.view = view;
	}

	public View findViewById(final int id) {
		return view.findViewById(id);
	}

	public Context getContext() {
		return view.getContext();
	}

	protected String getString(final int resId, final Object... formatArgs) {
		return getContext().getString(resId, formatArgs);
	}

}
