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

import android.graphics.BitmapRegionDecoder;

import com.qozix.tileview.TileView;
import com.qozix.tileview.graphics.BitmapDecoder;

/**
 * Created by mariotaku on 15/1/5.
 */
public abstract class AbsTileDecoder implements BitmapDecoder {

    public abstract void attachToTileView(TileView tileView);

    public abstract boolean isRecycled();

    public abstract boolean isSameDecoder(BitmapRegionDecoder decoder);

    public abstract void recycle();
}
