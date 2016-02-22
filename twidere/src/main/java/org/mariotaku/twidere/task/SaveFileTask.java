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

package org.mariotaku.twidere.task;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static android.text.TextUtils.isEmpty;

public abstract class SaveFileTask extends AsyncTask<Object, Object, SaveFileTask.SaveFileResult> implements Constants {

    private final WeakReference<Context> contextRef;

    @NonNull
    private final Uri source;
    @NonNull
    private final File destination;
    @NonNull
    private final FileInfoCallback getMimeType;

    public SaveFileTask(@NonNull final Context context, @NonNull final Uri source,
                        @NonNull final File destination, @NonNull final FileInfoCallback callback) {
        this.contextRef = new WeakReference<>(context);
        this.source = source;
        this.getMimeType = callback;
        this.destination = destination;
    }

    @Nullable
    public static SaveFileResult saveFile(@NonNull final Context context, @NonNull final Uri source,
                                          @NonNull final FileInfoCallback fileInfoCallback,
                                          @NonNull final File destinationDir) {
        final ContentResolver cr = context.getContentResolver();
        Source ioSrc = null;
        BufferedSink sink = null;
        try {
            String name = fileInfoCallback.getFilename(source);
            if (isEmpty(name)) return null;
            if (name.length() > 32) {
                name = name.substring(0, 32);
            }
            final String mimeType = fileInfoCallback.getMimeType(source);
            final String extension = fileInfoCallback.getExtension(mimeType);
            if (!destinationDir.isDirectory() && !destinationDir.mkdirs()) return null;
            String nameToSave = getFileNameWithExtension(name, extension);
            File saveFile = new File(destinationDir, nameToSave);
            if (saveFile.exists()) {
                nameToSave = getFileNameWithExtension(name + System.currentTimeMillis(), extension);
                saveFile = new File(destinationDir, nameToSave);
            }
            final InputStream in = cr.openInputStream(source);
            if (in == null) return null;
            ioSrc = Okio.source(in);
            sink = Okio.buffer(Okio.sink(saveFile));
            sink.writeAll(ioSrc);
            sink.flush();
            return new SaveFileResult(saveFile, mimeType);
        } catch (final IOException e) {
            Log.w(LOGTAG, "Failed to save file", e);
            return null;
        } finally {
            Utils.closeSilently(sink);
            Utils.closeSilently(ioSrc);
        }
    }

    @Override
    protected final SaveFileResult doInBackground(final Object... args) {
        final Context context = contextRef.get();
        if (context == null) return null;
        return saveFile(context, source, getMimeType, destination);
    }

    @Override
    protected void onCancelled() {
        dismissProgress();
    }

    @Override
    protected final void onPreExecute() {
        showProgress();
    }

    @Override
    protected final void onPostExecute(@Nullable final SaveFileResult result) {
        dismissProgress();
        if (result != null && result.savedFile != null) {
            onFileSaved(result.savedFile, result.mimeType);
        } else {
            onFileSaveFailed();
        }
    }

    protected abstract void onFileSaved(@NonNull File savedFile, @Nullable String mimeType);

    protected abstract void onFileSaveFailed();

    protected abstract void showProgress();

    protected abstract void dismissProgress();


    protected final Context getContext() {
        return contextRef.get();
    }

    private static String getFileNameWithExtension(String name, @Nullable String extension) {
        if (extension == null) return name;
        int lastDotIdx = name.lastIndexOf('.');
        if (lastDotIdx < 0) return name + "." + extension;
        return name.substring(0, lastDotIdx) + "." + extension;
    }

    public interface FileInfoCallback {
        @Nullable
        String getFilename(@NonNull Uri source);

        @Nullable
        String getMimeType(@NonNull Uri source);

        @Nullable
        String getExtension(@Nullable String mimeType);
    }

    public static final class SaveFileResult {
        File savedFile;
        String mimeType;

        public SaveFileResult(File savedFile, String mimeType) {
            this.savedFile = savedFile;
            this.mimeType = mimeType;
        }

        public File getSavedFile() {
            return savedFile;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

}
