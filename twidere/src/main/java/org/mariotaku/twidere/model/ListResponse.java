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

package org.mariotaku.twidere.model;

import android.os.Bundle;

import java.util.List;

public class ListResponse<Data> extends SingleResponse<List<Data>> {

	public final List<Data> list;

	public ListResponse(final List<Data> list, final Exception exception) {
		super(list, exception);
		this.list = list;
	}

	public ListResponse(final List<Data> list, final Exception exception, final Bundle extras) {
		super(list, exception, extras);
		this.list = list;
	}

}
