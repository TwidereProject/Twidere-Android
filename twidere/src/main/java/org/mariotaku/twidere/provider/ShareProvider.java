package org.mariotaku.twidere.provider;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.MediaColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import kotlin.collections.ArraysKt;
import kotlin.io.FilesKt;

/**
 * Created by mariotaku on 16/4/4.
 */
public class ShareProvider extends ContentProvider {
    public static final String[] COLUMNS = {MediaColumns.DATA, MediaColumns.DISPLAY_NAME,
            MediaColumns.SIZE, MediaColumns.MIME_TYPE};

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            final File file = getFile(uri);
            if (projection == null) {
                projection = COLUMNS;
            }
            MatrixCursor cursor = new MatrixCursor(projection, 1);
            Object[] values = new Object[projection.length];
            writeValue(projection, values, MediaColumns.DATA, file.getAbsolutePath());
            cursor.addRow(values);
            return cursor;
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!mode.equals("r")) throw new IllegalArgumentException();
        final File file = getFile(uri);
        return ParcelFileDescriptor.open(file,
                ParcelFileDescriptor.MODE_READ_ONLY);
    }

    private void writeValue(String[] columns, Object[] values, String column, Object value) {
        int idx = ArraysKt.indexOf(columns, column);
        if (idx >= 0) {
            values[idx] = value;
        }
    }

    private File getFile(@NonNull Uri uri) throws FileNotFoundException {
        final Context context = getContext();
        if (context == null) throw new IllegalStateException();
        final String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) throw new FileNotFoundException(uri.toString());
        File filesDir = getFilesDir(context);
        if (filesDir == null) throw new FileNotFoundException(uri.toString());
        try {
            filesDir = filesDir.getCanonicalFile();
            File file = new File(filesDir, lastPathSegment).getCanonicalFile();
            if (!FilesKt.startsWith(file, filesDir)) throw new SecurityException(uri.toString());
            return file;
        } catch (IOException e) {
            throw new FileNotFoundException(uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
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
    public static File getFilesDir(Context context) {
        File cacheDir = context.getCacheDir();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
            final File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null && externalCacheDir.canWrite()) {
                cacheDir = externalCacheDir;
            }
        }
        if (cacheDir == null) return null;
        return new File(cacheDir, "shared_files");
    }

    @Nullable
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        final File filesDir = getFilesDir(context);
        if (filesDir == null) return null;
        if (!filesDir.equals(file.getParentFile())) return null;
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority).appendPath(file.getName()).build();
    }

    public static boolean clearTempFiles(Context context) {
        final File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) return false;
        File[] files = externalCacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
        return true;
    }
}
