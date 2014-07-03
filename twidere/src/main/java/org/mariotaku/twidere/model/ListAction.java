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

public abstract class ListAction {

	private final int order;

	public ListAction(final int order) {
		this.order = order;
	}

	public abstract String getName();

	public int getOrder() {
		return order;
	}

	public String getSummary() {
		return null;
	}

	public void onClick() {

	}

	public boolean onLongClick() {
		return false;
	}

	@Override
	public final String toString() {
		return getName();
	}
}
