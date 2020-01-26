package org.mariotaku.twidere.model.util;

import android.location.Location;
import androidx.annotation.Nullable;

import org.mariotaku.microblog.library.twitter.model.GeoLocation;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.util.InternalParseUtils;

/**
 * Created by mariotaku on 16/3/8.
 */
public class ParcelableLocationUtils {
    private ParcelableLocationUtils() {
    }

    public static String getHumanReadableString(ParcelableLocation obj, int decimalDigits) {
        return String.format("%s,%s", InternalParseUtils.parsePrettyDecimal(obj.latitude, decimalDigits),
                InternalParseUtils.parsePrettyDecimal(obj.longitude, decimalDigits));
    }

    @Nullable
    public static ParcelableLocation fromGeoLocation(@Nullable GeoLocation geoLocation) {
        if (geoLocation == null) return null;
        final ParcelableLocation result = new ParcelableLocation();
        result.latitude = geoLocation.getLatitude();
        result.longitude = geoLocation.getLongitude();
        return result;
    }

    @Nullable
    public static ParcelableLocation fromLocation(@Nullable Location location) {
        if (location == null) return null;
        final ParcelableLocation result = new ParcelableLocation();
        result.latitude = location.getLatitude();
        result.longitude = location.getLongitude();
        return result;
    }

    public static boolean isValidLocation(final ParcelableLocation location) {
        return location != null && !Double.isNaN(location.latitude) && !Double.isNaN(location.longitude);
    }

    public static GeoLocation toGeoLocation(final ParcelableLocation location) {
        return isValidLocation(location) ? new GeoLocation(location.latitude, location.longitude) : null;
    }

    public static boolean isValidLocation(double latitude, double longitude) {
        return !Double.isNaN(latitude) && !Double.isNaN(longitude);
    }
}
