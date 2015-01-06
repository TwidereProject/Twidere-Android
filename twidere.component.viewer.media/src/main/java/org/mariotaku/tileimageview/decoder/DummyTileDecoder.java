/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.tileimageview.decoder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;

import com.qozix.tileview.TileView;

public final class DummyTileDecoder extends AbsTileDecoder {

    private final Bitmap mFallback;

    public DummyTileDecoder(Bitmap fallback) {
        mFallback = fallback;
    }

    @Override
    public void attachToTileView(final TileView tileView) {
        tileView.setTileDecoder(this);
        tileView.setDownsampleDecoder(this);
        tileView.resetDetailLevels();
        if (mFallback != null) {
            final int width = mFallback.getWidth(), height = mFallback.getHeight();
            tileView.setSize(width, height);
            tileView.addDetailLevel(1, "", "sample", width, height);
        } else {
            tileView.setSize(0, 0);
        }
    }

    @Override
    public Bitmap decode(final String fileName, final Context context) {
        return null;
    }

    @Override
    public boolean isRecycled() {
        return mFallback != null && mFallback.isRecycled();
    }

    @Override
    public boolean isSameDecoder(BitmapRegionDecoder decoder) {
        return false;
    }

    @Override
    public void recycle() {
        if (mFallback != null) {
            mFallback.recycle();
        }
    }
}