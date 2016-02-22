package org.mariotaku.twidere.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import com.j256.simplemagic.ContentInfoUtil;
import com.nostra13.universalimageloader.cache.disc.DiskCache;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.task.SaveFileTask;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.inject.Inject;

import okio.ByteString;

/**
 * Created by mariotaku on 16/1/1.
 */
public class CacheProvider extends ContentProvider {
    @Inject
    DiskCache mSimpleDiskCache;
    private ContentInfoUtil mContentInfoUtil;

    public static Uri getCacheUri(String key) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                .authority(TwidereConstants.AUTHORITY_TWIDERE_CACHE)
                .appendPath(ByteString.encodeUtf8(key).base64Url())
                .build();
    }

    public static String getCacheKey(Uri uri) {
        if (!ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()))
            throw new IllegalArgumentException(uri.toString());
        if (!TwidereConstants.AUTHORITY_TWIDERE_CACHE.equals(uri.getAuthority()))
            throw new IllegalArgumentException(uri.toString());
        return ByteString.decodeBase64(uri.getLastPathSegment()).utf8();
    }

    @Override
    public boolean onCreate() {
        final Context context = getContext();
        assert context != null;
        mContentInfoUtil = new ContentInfoUtil();
        GeneralComponentHelper.build(context).inject(this);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final File file = mSimpleDiskCache.get(getCacheKey(uri));
        if (file == null) return null;
        try {
            return mContentInfoUtil.findMatch(file).getMimeType();
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        try {
            final File file = mSimpleDiskCache.get(getCacheKey(uri));
            if (file == null) throw new FileNotFoundException();
            final int modeBits = modeToMode(mode);
            if (modeBits != ParcelFileDescriptor.MODE_READ_ONLY)
                throw new IllegalArgumentException("Cache can't be opened for write");
            return ParcelFileDescriptor.open(file, modeBits);
        } catch (IOException e) {
            throw new FileNotFoundException();
        }
    }

    /**
     * Copied from ContentResolver.java
     */
    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE
                    | ParcelFileDescriptor.MODE_CREATE
                    | ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    public static final class CacheFileTypeCallback implements SaveFileTask.FileInfoCallback {
        private final Context context;

        public CacheFileTypeCallback(Context context) {
            this.context = context;
        }

        @Nullable
        @Override
        public String getFilename(@NonNull Uri source) {
            final String cacheKey = getCacheKey(source);
            if (cacheKey == null) return null;
            return cacheKey.replaceAll("[^\\w\\d_]", "_");
        }

        @Override
        public String getMimeType(@NonNull Uri source) {
            return context.getContentResolver().getType(source);
        }

        @Override
        public String getExtension(String mimeType) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
    }
}
