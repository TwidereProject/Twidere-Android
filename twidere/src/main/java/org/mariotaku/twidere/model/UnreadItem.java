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

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;

public class UnreadItem implements JSONParcelable {

	public static final JSONParcelable.Creator<UnreadItem> JSON_CREATOR = new JSONParcelable.Creator<UnreadItem>() {
		@Override
		public UnreadItem createFromParcel(final JSONParcel in) {
			return new UnreadItem(in);
		}

		@Override
		public UnreadItem[] newArray(final int size) {
			return new UnreadItem[size];
		}
	};

	public final long id, account_id;

	public UnreadItem(final JSONParcel in) {
		id = in.readLong("id");
		account_id = in.readLong("account_id");
	}

	public UnreadItem(final long id, final long account_id) {
		this.id = id;
		this.account_id = account_id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof UnreadItem)) return false;
		final UnreadItem other = (UnreadItem) obj;
		if (account_id != other.account_id) return false;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "UnreadItem{id=" + id + ", account_id=" + account_id + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeLong("id", id);
		out.writeLong("account_id", account_id);
	}
}
