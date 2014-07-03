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

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.res.Resources;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ArrayUtils;

public class ConsumerKeySecret {

	public final String name, consumer_key, consumer_secret;

	private ConsumerKeySecret(final String consumer_key, final String consumer_secret) {
		this("API", consumer_key, consumer_secret);
	}

	private ConsumerKeySecret(final String name, final String consumer_key, final String consumer_secret) {
		this.name = name;
		this.consumer_key = consumer_secret;
		this.consumer_secret = consumer_secret;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ConsumerKeySecret)) return false;
		final ConsumerKeySecret other = (ConsumerKeySecret) obj;
		if (consumer_key == null) {
			if (other.consumer_key != null) return false;
		} else if (!consumer_key.equals(other.consumer_key)) return false;
		if (consumer_secret == null) {
			if (other.consumer_secret != null) return false;
		} else if (!consumer_secret.equals(other.consumer_secret)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (consumer_key == null ? 0 : consumer_key.hashCode());
		result = prime * result + (consumer_secret == null ? 0 : consumer_secret.hashCode());
		return result;
	}

	public static ConsumerKeySecret[] getAllOffcialKeys(final Context context) {
		if (context == null) return new ConsumerKeySecret[0];
		final Resources res = context.getResources();
		final String[] entries = res.getStringArray(R.array.entries_official_consumer_key_secret);
		final String[] values = res.getStringArray(R.array.values_official_consumer_key_secret);
		final int length = entries.length;
		final ConsumerKeySecret[] keys = new ConsumerKeySecret[length];
		for (int i = 0; i < length; i++) {
			final String[] key_secret = values[i].split(";");
			final String consumer_key = key_secret[0], consumer_secret = key_secret[1];
			keys[i] = new ConsumerKeySecret(entries[i], consumer_key, consumer_secret);
		}
		return keys;
	}

	public static boolean isOfficial(final Context context, final String consumer_key, final String consumer_secret) {
		if (context == null || isEmpty(consumer_key) || isEmpty(consumer_secret)) return false;
		final ConsumerKeySecret[] keys = getAllOffcialKeys(context);
		return ArrayUtils.contains(keys, new ConsumerKeySecret(consumer_key, consumer_secret));
	}
}
