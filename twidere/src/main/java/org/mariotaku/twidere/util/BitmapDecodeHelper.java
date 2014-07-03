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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;

public class BitmapDecodeHelper {

	public static Bitmap decode(final FileDescriptor fd, final BitmapFactory.Options opts) {
		if (fd == null) return null;
		final int orientation = Exif.getOrientation(new FileInputStream(fd));
		final Bitmap bm = BitmapFactory.decodeFileDescriptor(fd, null, opts);
		final Matrix m = new Matrix();
		switch (orientation) {
			case 270:
			case 90:
				m.postRotate(orientation);
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
			case 180:
				m.postRotate(orientation);
				m.postScale(bm.getWidth(), bm.getHeight());
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
		}
		return bm;
	}

	public static Bitmap decode(final InputStream is, final BitmapFactory.Options opts) {
		if (is == null) return null;
		final int orientation = Exif.getOrientation(is);
		final Bitmap bm = BitmapFactory.decodeStream(is, null, opts);
		final Matrix m = new Matrix();
		switch (orientation) {
			case 270:
			case 90:
				m.postRotate(orientation);
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
			case 180:
				m.postRotate(orientation);
				m.postScale(bm.getWidth(), bm.getHeight());
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
		}
		return bm;
	}

	public static Bitmap decode(final String path, final BitmapFactory.Options opts) {
		if (path == null || opts == null) return null;
		final Bitmap bm = BitmapFactory.decodeFile(path, opts);
		if (bm == null) return null;
		final Matrix m = new Matrix();
		final int orientation = Exif.getOrientation(path);
		switch (orientation) {
			case 270:
			case 90:
				m.postRotate(orientation);
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
			case 180:
				m.postRotate(orientation);
				m.postScale(bm.getWidth(), bm.getHeight());
				return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
		}
		return bm;
	}

}
