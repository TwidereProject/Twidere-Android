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

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static android.text.TextUtils.isEmpty;

public abstract class SaveFileTask extends AsyncTask<Object, Object, SaveFileTask.SaveFileResult> implements Constants {

    private final WeakReference<Context> contextRef;

    @NonNull
    private final File source, destination;
    @NonNull
    private final MimeTypeCallback getMimeType;

    public SaveFileTask(@NonNull final Context context, @NonNull final File source,
                        @NonNull final File destination, @NonNull final MimeTypeCallback getMimeType) {
        this.contextRef = new WeakReference<>(context);
        this.source = source;
        this.getMimeType = getMimeType;
        this.destination = destination;
    }

    @Nullable
    public static SaveFileResult saveFile(@NonNull final Context context, @NonNull final File source,
                                          @NonNull final MimeTypeCallback mimeTypeCallback,
                                          @NonNull final File destinationDir) {
        Source ioSrc = null;
        BufferedSink sink = null;
        try {
            final String name = source.getName();
            if (isEmpty(name)) return null;
            final String mimeType = mimeTypeCallback.getMimeType(source);
            final String extension = mimeTypeCallback.getExtension(mimeType);
            if (extension == null) return null;
            final String nameToSave = getFileNameWithExtension(name, extension);
            if (!destinationDir.isDirectory() && !destinationDir.mkdirs()) return null;
            final File saveFile = new File(destinationDir, nameToSave);
            ioSrc = Okio.source(source);
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
        if (result != null) {
            onFileSaved(result.savedFile, result.mimeType);
        }
    }

    protected abstract void onFileSaved(File savedFile, String mimeType);

    protected abstract void showProgress();

    protected abstract void dismissProgress();


    protected final Context getContext() {
        return contextRef.get();
    }

    private static String getFileNameWithExtension(String name, String extension) {
        int lastDotIdx = name.lastIndexOf('.');
        if (lastDotIdx < 0) return name + "." + extension;
        return name.substring(0, lastDotIdx) + "." + extension;
    }

    public interface MimeTypeCallback {
        String getMimeType(File source);

        String getExtension(String mimeType);
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

    public static class StringMimeTypeCallback implements MimeTypeCallback {
        private final String mimeType;

        public StringMimeTypeCallback(String mimeType) {
            this.mimeType = mimeType;
        }

        @Override
        public String getMimeType(File source) {
            return mimeType;
        }

        @Override
        public String getExtension(String mimeType) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
    }
}
