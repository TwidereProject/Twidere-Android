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

package org.mariotaku.twidere.util;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;

import java.io.File;
import java.io.IOException;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import static android.text.TextUtils.isEmpty;

public class SaveFileTask extends AsyncTask<Object, Object, File> implements Constants {

    private static final String PROGRESS_FRAGMENT_TAG = "progress";

    private final File source, destination;
    private final Activity activity;
    private final String mimeType;

    public SaveFileTask(final Activity activity, final File source, final String mimeType, final File destination) {
        this.activity = activity;
        this.source = source;
        this.mimeType = mimeType;
        this.destination = destination;
    }

    public static SaveFileTask saveImage(final Activity activity, final File source) {
        final String mimeType = Utils.getImageMimeType(source);
        final MimeTypeMap map = MimeTypeMap.getSingleton();
        final String extension = map.getExtensionFromMimeType(mimeType);
        if (extension == null) return null;
        final File pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File saveDir = new File(pubDir, "Twidere");
        return new SaveFileTask(activity, source, mimeType, saveDir);
    }

    public static File saveFile(final Context context, final File source, final String mimeType, final File destination) {
        if (context == null && source == null) return null;
        Source ioSrc = null;
        BufferedSink sink = null;
        try {
            final String name = source.getName();
            if (isEmpty(name)) return null;
            final MimeTypeMap map = MimeTypeMap.getSingleton();
            final String extension = map.getExtensionFromMimeType(mimeType);
            if (extension == null) return null;
            final String nameToSave = getFileNameWithExtension(name, extension);
            if (!destination.isDirectory() && !destination.mkdirs()) return null;
            final File saveFile = new File(destination, nameToSave);
            ioSrc = Okio.source(source);
            sink = Okio.buffer(Okio.sink(saveFile));
            sink.writeAll(ioSrc);
            sink.flush();
            if (mimeType != null) {
                MediaScannerConnection.scanFile(context, new String[]{saveFile.getPath()},
                        new String[]{mimeType}, null);
            }
            return saveFile;
        } catch (final IOException e) {
            final int errno = Utils.getErrorNo(e.getCause());
            Log.w(LOGTAG, "Failed to save file", e);
            return null;
        } finally {
            Utils.closeSilently(sink);
            Utils.closeSilently(ioSrc);
        }
    }

    private static String getFileNameWithExtension(String name, String extension) {
        int lastDotIdx = name.lastIndexOf('.');
        if (lastDotIdx < 0) return name + "." + extension;
        return name.substring(0, lastDotIdx) + "." + extension;
    }

    @Override
    protected File doInBackground(final Object... args) {
        if (source == null) return null;
        return saveFile(activity, source, mimeType, destination);
    }

    @Override
    protected void onCancelled() {
        final FragmentManager fm = activity.getFragmentManager();
        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
        if (fragment != null && fragment.isVisible()) {
            fragment.dismiss();
        }
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(final File result) {
        final FragmentManager fm = activity.getFragmentManager();
        final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.dismiss();
        }
        super.onPostExecute(result);
        if (result != null && result.exists()) {
            Toast.makeText(activity, R.string.saved_to_gallery, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPreExecute() {
        final DialogFragment fragment = new ProgressDialogFragment();
        fragment.setCancelable(false);
        fragment.show(activity.getFragmentManager(), PROGRESS_FRAGMENT_TAG);
        super.onPreExecute();
    }

}
