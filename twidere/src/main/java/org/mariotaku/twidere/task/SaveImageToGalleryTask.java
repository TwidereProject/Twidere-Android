/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;

import java.io.File;

/**
 * Created by mariotaku on 15/12/28.
 */
public class SaveImageToGalleryTask extends ProgressSaveFileTask {

    public SaveImageToGalleryTask(@NonNull Activity activity, @NonNull File source, @NonNull File destination) {
        super(activity, source, destination, new ImageMimeTypeCallback());
    }

    public static SaveFileTask create(final Activity activity, final File source) {
        final File pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        final File saveDir = new File(pubDir, "Twidere");
        return new SaveImageToGalleryTask(activity, source, saveDir);
    }

    protected void onFileSaved(File savedFile, String mimeType) {
        final Context context = getContext();
        if (context == null) return;
        if (savedFile != null && savedFile.exists()) {
            MediaScannerConnection.scanFile(context, new String[]{savedFile.getPath()},
                    new String[]{mimeType}, null);
            Toast.makeText(context, R.string.saved_to_gallery, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    public static final class ImageMimeTypeCallback implements MimeTypeCallback {
        @Override
        public String getMimeType(File source) {
            return Utils.getImageMimeType(source);
        }

        @Override
        public String getExtension(String mimeType) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        }
    }
}
