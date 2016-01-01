package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.net.Uri;

import org.mariotaku.twidere.TwidereConstants;

import okio.ByteString;

/**
 * Created by mariotaku on 16/1/1.
 */
public class SimpleDiskCacheUtils implements TwidereConstants {

    public static Uri getCacheUri(String key) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(AUTHORITY_TWIDERE_CACHE)
                .appendPath(ByteString.encodeUtf8(key).base64Url())
                .build();
    }

    public static String getCacheKey(Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()))
            throw new IllegalArgumentException(uri.toString());
        if (!AUTHORITY_TWIDERE_CACHE.equals(uri.getAuthority()))
            throw new IllegalArgumentException(uri.toString());
        return ByteString.decodeBase64(uri.getLastPathSegment()).utf8();
    }
}
