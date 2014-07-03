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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.formatDirectMessageText;
import static org.mariotaku.twidere.util.content.ContentValuesUtils.getAsBoolean;
import static org.mariotaku.twidere.util.content.ContentValuesUtils.getAsLong;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.ParseUtils;

import twitter4j.DirectMessage;
import twitter4j.User;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

public class ParcelableDirectMessage implements Parcelable, Serializable, Comparable<ParcelableDirectMessage> {

	private static final long serialVersionUID = -3721836808981416526L;

	public static final Parcelable.Creator<ParcelableDirectMessage> CREATOR = new Parcelable.Creator<ParcelableDirectMessage>() {
		@Override
		public ParcelableDirectMessage createFromParcel(final Parcel in) {
			return new ParcelableDirectMessage(in);
		}

		@Override
		public ParcelableDirectMessage[] newArray(final int size) {
			return new ParcelableDirectMessage[size];
		}
	};
	public static final Comparator<ParcelableDirectMessage> MESSAGE_ID_COMPARATOR = new Comparator<ParcelableDirectMessage>() {

		@Override
		public int compare(final ParcelableDirectMessage object1, final ParcelableDirectMessage object2) {
			final long diff = object2.id - object1.id;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public final long account_id, id, timestamp;

	public final long sender_id, recipient_id;

	public final boolean is_outgoing;

	public final String text_html, text_plain, text_unescaped;

	public final String sender_name, recipient_name, sender_screen_name, recipient_screen_name;

	public final String sender_profile_image_url, recipient_profile_image_url;

	public final String first_media;

	public final ParcelableMedia[] medias;

	public ParcelableDirectMessage(final ContentValues values) {
		text_plain = values.getAsString(DirectMessages.TEXT_PLAIN);
		text_html = values.getAsString(DirectMessages.TEXT_HTML);
		text_unescaped = toPlainText(text_html);
		sender_screen_name = values.getAsString(DirectMessages.SENDER_SCREEN_NAME);
		sender_profile_image_url = values.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL);
		sender_name = values.getAsString(DirectMessages.SENDER_NAME);
		sender_id = getAsLong(values, DirectMessages.SENDER_ID, -1);
		recipient_screen_name = values.getAsString(DirectMessages.RECIPIENT_SCREEN_NAME);
		recipient_profile_image_url = values.getAsString(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL);
		recipient_name = values.getAsString(DirectMessages.RECIPIENT_NAME);
		recipient_id = getAsLong(values, DirectMessages.RECIPIENT_ID, -1);
		timestamp = getAsLong(values, DirectMessages.MESSAGE_TIMESTAMP, -1);
		id = getAsLong(values, DirectMessages.MESSAGE_ID, -1);
		is_outgoing = getAsBoolean(values, DirectMessages.IS_OUTGOING, false);
		account_id = getAsLong(values, DirectMessages.ACCOUNT_ID, -1);
		medias = ParcelableMedia.fromJSONString(values.getAsString(DirectMessages.MEDIAS));
		first_media = values.getAsString(DirectMessages.FIRST_MEDIA);
	}

	public ParcelableDirectMessage(final Cursor c, final CursorIndices idx) {
		account_id = idx.account_id != -1 ? c.getLong(idx.account_id) : -1;
		is_outgoing = idx.is_outgoing != -1 ? c.getShort(idx.is_outgoing) == 1 : null;
		id = idx.message_id != -1 ? c.getLong(idx.message_id) : -1;
		timestamp = idx.message_timestamp != -1 ? c.getLong(idx.message_timestamp) : -1;
		sender_id = idx.sender_id != -1 ? c.getLong(idx.sender_id) : -1;
		recipient_id = idx.recipient_id != -1 ? c.getLong(idx.recipient_id) : -1;
		text_html = idx.text != -1 ? c.getString(idx.text) : null;
		text_plain = idx.text_plain != -1 ? c.getString(idx.text_plain) : null;
		text_unescaped = toPlainText(text_html);
		sender_name = idx.sender_name != -1 ? c.getString(idx.sender_name) : null;
		recipient_name = idx.recipient_name != -1 ? c.getString(idx.recipient_name) : null;
		sender_screen_name = idx.sender_screen_name != -1 ? c.getString(idx.sender_screen_name) : null;
		recipient_screen_name = idx.recipient_screen_name != -1 ? c.getString(idx.recipient_screen_name) : null;
		sender_profile_image_url = idx.sender_profile_image_url != -1 ? c.getString(idx.sender_profile_image_url)
				: null;
		recipient_profile_image_url = idx.recipient_profile_image_url != -1 ? c
				.getString(idx.recipient_profile_image_url) : null;
		medias = ParcelableMedia.fromJSONString(idx.medias != -1 ? c.getString(idx.medias) : null);
		first_media = idx.first_media != -1 ? c.getString(idx.first_media) : null;
	}

	public ParcelableDirectMessage(final DirectMessage message, final long account_id, final boolean is_outgoing) {
		this.account_id = account_id;
		this.is_outgoing = is_outgoing;
		final User sender = message.getSender(), recipient = message.getRecipient();
		final String sender_profile_image_url_string = sender != null ? ParseUtils.parseString(sender
				.getProfileImageUrlHttps()) : null;
		final String recipient_profile_image_url_string = recipient != null ? ParseUtils.parseString(recipient
				.getProfileImageUrlHttps()) : null;
		id = message.getId();
		timestamp = getTime(message.getCreatedAt());
		sender_id = sender != null ? sender.getId() : -1;
		recipient_id = recipient != null ? recipient.getId() : -1;
		text_html = formatDirectMessageText(message);
		text_plain = message.getText();
		sender_name = sender != null ? sender.getName() : null;
		recipient_name = recipient != null ? recipient.getName() : null;
		sender_screen_name = sender != null ? sender.getScreenName() : null;
		recipient_screen_name = recipient != null ? recipient.getScreenName() : null;
		sender_profile_image_url = sender_profile_image_url_string;
		recipient_profile_image_url = recipient_profile_image_url_string;
		text_unescaped = toPlainText(text_html);
		medias = ParcelableMedia.fromEntities(message);
		first_media = medias != null && medias.length > 0 ? medias[0].url : null;
	}

	public ParcelableDirectMessage(final Parcel in) {
		account_id = in.readLong();
		id = in.readLong();
		timestamp = in.readLong();
		sender_id = in.readLong();
		recipient_id = in.readLong();
		is_outgoing = in.readInt() == 1;
		text_html = in.readString();
		text_plain = in.readString();
		sender_name = in.readString();
		recipient_name = in.readString();
		sender_screen_name = in.readString();
		recipient_screen_name = in.readString();
		sender_profile_image_url = in.readString();
		recipient_profile_image_url = in.readString();
		text_unescaped = in.readString();
		medias = in.createTypedArray(ParcelableMedia.CREATOR);
		first_media = medias != null && medias.length > 0 ? medias[0].url : null;
	}

	@Override
	public int compareTo(final ParcelableDirectMessage another) {
		if (another == null) return 0;
		final long diff = another.id - id;
		if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) diff;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ParcelableDirectMessage)) return false;
		final ParcelableDirectMessage other = (ParcelableDirectMessage) obj;
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
		return "ParcelableDirectMessage{account_id=" + account_id + ", id=" + id + ", timestamp=" + timestamp
				+ ", sender_id=" + sender_id + ", recipient_id=" + recipient_id + ", is_outgoing=" + is_outgoing
				+ ", text_html=" + text_html + ", text_plain=" + text_plain + ", text_unescaped=" + text_unescaped
				+ ", sender_name=" + sender_name + ", recipient_name=" + recipient_name + ", sender_screen_name="
				+ sender_screen_name + ", recipient_screen_name=" + recipient_screen_name
				+ ", sender_profile_image_url=" + sender_profile_image_url + ", recipient_profile_image_url="
				+ recipient_profile_image_url + "}";
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(account_id);
		out.writeLong(id);
		out.writeLong(timestamp);
		out.writeLong(sender_id);
		out.writeLong(recipient_id);
		out.writeInt(is_outgoing ? 1 : 0);
		out.writeString(text_html);
		out.writeString(text_plain);
		out.writeString(sender_name);
		out.writeString(recipient_name);
		out.writeString(sender_screen_name);
		out.writeString(recipient_screen_name);
		out.writeString(sender_profile_image_url);
		out.writeString(recipient_profile_image_url);
		out.writeString(text_unescaped);
		out.writeTypedArray(medias, flags);
	}

	private static long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}

	public static class CursorIndices {

		public final int account_id, message_id, message_timestamp, sender_name, sender_screen_name, text, text_plain,
				recipient_name, recipient_screen_name, sender_profile_image_url, is_outgoing,
				recipient_profile_image_url, sender_id, recipient_id, medias, first_media;

		public CursorIndices(final Cursor cursor) {
			account_id = cursor.getColumnIndex(DirectMessages.ACCOUNT_ID);
			message_id = cursor.getColumnIndex(DirectMessages.MESSAGE_ID);
			message_timestamp = cursor.getColumnIndex(DirectMessages.MESSAGE_TIMESTAMP);
			sender_id = cursor.getColumnIndex(DirectMessages.SENDER_ID);
			recipient_id = cursor.getColumnIndex(DirectMessages.RECIPIENT_ID);
			is_outgoing = cursor.getColumnIndex(DirectMessages.IS_OUTGOING);
			text = cursor.getColumnIndex(DirectMessages.TEXT_HTML);
			text_plain = cursor.getColumnIndex(DirectMessages.TEXT_PLAIN);
			sender_name = cursor.getColumnIndex(DirectMessages.SENDER_NAME);
			recipient_name = cursor.getColumnIndex(DirectMessages.RECIPIENT_NAME);
			sender_screen_name = cursor.getColumnIndex(DirectMessages.SENDER_SCREEN_NAME);
			recipient_screen_name = cursor.getColumnIndex(DirectMessages.RECIPIENT_SCREEN_NAME);
			sender_profile_image_url = cursor.getColumnIndex(DirectMessages.SENDER_PROFILE_IMAGE_URL);
			recipient_profile_image_url = cursor.getColumnIndex(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL);
			medias = cursor.getColumnIndex(DirectMessages.MEDIAS);
			first_media = cursor.getColumnIndex(DirectMessages.FIRST_MEDIA);
		}
	}
}
