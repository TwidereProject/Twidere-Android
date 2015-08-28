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

package org.mariotaku.twidere.util.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.BaseDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mariotaku on 15/8/28.
 */
public class ReadOnlyDiskLRUNameCache extends BaseDiskCache {
    public ReadOnlyDiskLRUNameCache(File cacheDir) {
        super(cacheDir);
    }

    public ReadOnlyDiskLRUNameCache(File cacheDir, File reserveCacheDir) {
        super(cacheDir, reserveCacheDir);
    }

    @Override
    public boolean save(String imageUri, InputStream imageStream, IoUtils.CopyListener listener) throws IOException {
        return false;
    }

    @Override
    public boolean save(String imageUri, Bitmap bitmap) throws IOException {
        return false;
    }

    @Override
    public boolean remove(String imageUri) {
        return false;
    }

    @Override
    public void clear() {
        // No-op
    }

    @Override
    protected File getFile(String imageUri) {
        String fileName = fileNameGenerator.generate(imageUri) + ".0";
        File dir = cacheDir;
        if ((!cacheDir.exists()) && (!cacheDir.mkdirs()) &&
                (reserveCacheDir != null) && ((reserveCacheDir.exists()) || (reserveCacheDir.mkdirs()))) {
            dir = reserveCacheDir;
        }
        return new File(dir, fileName);
    }

    public ReadOnlyDiskLRUNameCache(File cacheDir, File reserveCacheDir, FileNameGenerator fileNameGenerator) {
        super(cacheDir, reserveCacheDir, fileNameGenerator);
    }
}
