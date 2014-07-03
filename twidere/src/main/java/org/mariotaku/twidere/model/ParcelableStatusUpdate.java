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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class ParcelableStatusUpdate implements Parcelable {

	public static final Parcelable.Creator<ParcelableStatusUpdate> CREATOR = new Parcelable.Creator<ParcelableStatusUpdate>() {
		@Override
		public ParcelableStatusUpdate createFromParcel(final Parcel in) {
			return new ParcelableStatusUpdate(in);
		}

		@Override
		public ParcelableStatusUpdate[] newArray(final int size) {
			return new ParcelableStatusUpdate[size];
		}
	};

	public final Account[] accounts;
	public final ParcelableMediaUpdate[] medias;
	public final String text;
	public final ParcelableLocation location;
	public final long in_reply_to_status_id;
	public final boolean is_possibly_sensitive;

	/**
	 * @deprecated It has too much arguments to call, use
	 *             <b>ParcelableStatusUpdate.Builder</b> instead.
	 */
	@Deprecated
	public ParcelableStatusUpdate(final Account[] accounts, final String text, final ParcelableLocation location,
			final ParcelableMediaUpdate[] medias, final long in_reply_to_status_id, final boolean is_possibly_sensitive) {
		this.accounts = accounts;
		this.text = text;
		this.location = location;
		this.medias = medias;
		this.in_reply_to_status_id = in_reply_to_status_id;
		this.is_possibly_sensitive = is_possibly_sensitive;
	}

	public ParcelableStatusUpdate(final Context context, final DraftItem draft) {
		accounts = Account.getAccounts(context, draft.account_ids);
		text = draft.text;
		location = draft.location;
		medias = draft.medias;
		in_reply_to_status_id = draft.in_reply_to_status_id;
		is_possibly_sensitive = draft.is_possibly_sensitive;
	}

	public ParcelableStatusUpdate(final Parcel in) {
		accounts = in.createTypedArray(Account.CREATOR);
		text = in.readString();
		location = in.readParcelable(ParcelableLocation.class.getClassLoader());
		medias = in.createTypedArray(ParcelableMediaUpdate.CREATOR);
		in_reply_to_status_id = in.readLong();
		is_possibly_sensitive = in.readInt() == 1;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "ParcelableStatusUpdate{accounts=" + Arrays.toString(accounts) + ", medias=" + Arrays.toString(medias)
				+ ", text=" + text + ", location=" + location + ", in_reply_to_status_id=" + in_reply_to_status_id
				+ ", is_possibly_sensitive=" + is_possibly_sensitive + "}";
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeTypedArray(accounts, flags);
		dest.writeString(text);
		dest.writeParcelable(location, flags);
		dest.writeTypedArray(medias, flags);
		dest.writeLong(in_reply_to_status_id);
		dest.writeInt(is_possibly_sensitive ? 1 : 0);
	}

	public static final class Builder {

		private Account[] accounts;
		private String text;
		private ParcelableLocation location;
		private ParcelableMediaUpdate[] medias;
		private long in_reply_to_status_id;
		private boolean is_possibly_sensitive;

		public Builder() {

		}

		public Builder(final ParcelableStatusUpdate base) {
			accounts(base.accounts);
			text(base.text);
			medias(base.medias);
			location(base.location);
			inReplyToStatusId(base.in_reply_to_status_id);
			isPossiblySensitive(base.is_possibly_sensitive);
		}

		public Builder accounts(final Account[] accounts) {
			this.accounts = accounts;
			return this;
		}

		public ParcelableStatusUpdate build() {
			return new ParcelableStatusUpdate(accounts, text, location, medias, in_reply_to_status_id,
					is_possibly_sensitive);
		}

		public Builder inReplyToStatusId(final long in_reply_to_status_id) {
			this.in_reply_to_status_id = in_reply_to_status_id;
			return this;
		}

		public Builder isPossiblySensitive(final boolean is_possibly_sensitive) {
			this.is_possibly_sensitive = is_possibly_sensitive;
			return this;
		}

		public Builder location(final ParcelableLocation location) {
			this.location = location;
			return this;
		}

		public Builder medias(final ParcelableMediaUpdate... medias) {
			this.medias = medias;
			return this;
		}

		public Builder text(final String text) {
			this.text = text;
			return this;
		}
	}

}
