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
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.CacheProvider;

import java.io.File;

/**
 * Created by mariotaku on 15/12/28.
 */
public class SaveMediaToGalleryTask extends ProgressSaveFileTask {

    public SaveMediaToGalleryTask(@NonNull Activity activity, @NonNull Uri source, @NonNull File destination, String type) {
        super(activity, source, destination, new CacheProvider.CacheFileTypeCallback(activity, type));
    }

    public static SaveFileTask create(final Activity activity, final Uri source,
                                      @NonNull @CacheProvider.Type final String type) {
        final File pubDir;
        switch (type) {
            case CacheProvider.Type.VIDEO: {
                pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                break;
            }
            case CacheProvider.Type.IMAGE: {
                pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                break;
            }
            default: {
                pubDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                break;
            }
        }
        final File saveDir = new File(pubDir, "Twidere");
        return new SaveMediaToGalleryTask(activity, source, saveDir, type);
    }

    @Override
    protected void onFileSaved(@NonNull File savedFile, @Nullable String mimeType) {
        final Context context = getContext();
        if (context == null) return;
        MediaScannerConnection.scanFile(context, new String[]{savedFile.getPath()},
                new String[]{mimeType}, null);
        Toast.makeText(context, R.string.saved_to_gallery, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onFileSaveFailed() {
        final Context context = getContext();
        if (context == null) return;
        Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
    }

}
