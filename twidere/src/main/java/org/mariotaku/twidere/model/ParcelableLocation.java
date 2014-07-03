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

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.util.ParseUtils;

import twitter4j.GeoLocation;

import java.io.Serializable;

public class ParcelableLocation implements Serializable, Parcelable, JSONParcelable {

	private static final long serialVersionUID = -1690848439775407442L;

	public final double latitude, longitude;

	public static final Parcelable.Creator<ParcelableLocation> CREATOR = new Parcelable.Creator<ParcelableLocation>() {
		@Override
		public ParcelableLocation createFromParcel(final Parcel in) {
			return new ParcelableLocation(in);
		}

		@Override
		public ParcelableLocation[] newArray(final int size) {
			return new ParcelableLocation[size];
		}
	};

	public static final JSONParcelable.Creator<ParcelableLocation> JSON_CREATOR = new JSONParcelable.Creator<ParcelableLocation>() {
		@Override
		public ParcelableLocation createFromParcel(final JSONParcel in) {
			return new ParcelableLocation(in);
		}

		@Override
		public ParcelableLocation[] newArray(final int size) {
			return new ParcelableLocation[size];
		}
	};

	public ParcelableLocation(final double latitude, final double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public ParcelableLocation(final GeoLocation location) {
		latitude = location != null ? location.getLatitude() : -1;
		longitude = location != null ? location.getLongitude() : -1;
	}

	public ParcelableLocation(final JSONParcel in) {
		latitude = in.readDouble("latitude", -1);
		longitude = in.readDouble("longutude", -1);
	}

	public ParcelableLocation(final Location location) {
		latitude = location != null ? location.getLatitude() : -1;
		longitude = location != null ? location.getLongitude() : -1;
	}

	public ParcelableLocation(final Parcel in) {
		latitude = in.readDouble();
		longitude = in.readDouble();
	}

	public ParcelableLocation(final String location_string) {
		if (location_string == null) {
			latitude = -1;
			longitude = -1;
			return;
		}
		final String[] longlat = location_string.split(",");
		if (longlat == null || longlat.length != 2) {
			latitude = -1;
			longitude = -1;
		} else {
			latitude = ParseUtils.parseDouble(longlat[0]);
			longitude = ParseUtils.parseDouble(longlat[1]);
		}
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ParcelableLocation)) return false;
		final ParcelableLocation other = (ParcelableLocation) obj;
		if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude)) return false;
		if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(latitude);
		result = prime * result + (int) (temp ^ temp >>> 32);
		temp = Double.doubleToLongBits(longitude);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	public boolean isValid() {
		return latitude >= 0 || longitude >= 0;
	}

	public GeoLocation toGeoLocation() {
		return isValid() ? new GeoLocation(latitude, longitude) : null;
	}

	@Override
	public String toString() {
		return "ParcelableLocation{latitude=" + latitude + ", longitude=" + longitude + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeDouble("latitude", latitude);
		out.writeDouble("longitude", longitude);
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeDouble(latitude);
		out.writeDouble(longitude);
	}

	public static ParcelableLocation fromString(final String string) {
		final ParcelableLocation location = new ParcelableLocation(string);
		if (ParcelableLocation.isValidLocation(location)) return location;
		return null;
	}

	public static boolean isValidLocation(final ParcelableLocation location) {
		return location != null && location.isValid();
	}

	public static GeoLocation toGeoLocation(final ParcelableLocation location) {
		return isValidLocation(location) ? location.toGeoLocation() : null;
	}

	public static String toString(final ParcelableLocation location) {
		if (location == null) return null;
		return location.latitude + "," + location.longitude;
	}
}
