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
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

import com.qozix.tileview.TileView;
import com.qozix.tileview.graphics.BitmapDecoder;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Display an image with {@link com.qozix.tileview.TileView} using {@link android.graphics.BitmapRegionDecoder} <br/>
 * <br/>
 * <b>Usage:</b> <blockquote> BitmapRegionTileDecoder decoder = new
 * BitmapRegionTileDecoder("/path/to/image.jpg", true); <br/>
 * decoder.attachToTileView(tileView); </blockquote>
 *
 * @author mariotaku
 */
@SuppressWarnings("unused")
public final class BitmapRegionTileDecoder extends AbsTileDecoder {

    private static final Pattern PATTERN_TILE_FORMAT = Pattern.compile("(\\d+):(\\d+)\\-(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private final BitmapRegionDecoder mDecoder;
    private final int mTileSize;

    private final DownSampleDecoder mDownSampleDecoder;

    public BitmapRegionTileDecoder(final BitmapRegionDecoder decoder) {
        this(decoder, 128, 512);
    }

    /**
     * @param decoder           {@link android.graphics.BitmapRegionDecoder} instance
     * @param tileSize          size of each tile
     * @param maxDownSampleSize max size of downsample image
     */
    public BitmapRegionTileDecoder(final BitmapRegionDecoder decoder, final int tileSize, final int maxDownSampleSize) {
        if (decoder == null) throw new NullPointerException();
        mDecoder = decoder;
        mTileSize = tileSize;
        final int downsampleInSampleSize = nextPowerOfTwo(Math.max(1, Math.max(getWidth(), getHeight())
                / maxDownSampleSize));
        mDownSampleDecoder = new DownSampleDecoder(decoder, downsampleInSampleSize);
    }

    public BitmapRegionTileDecoder(final FileDescriptor fd, final boolean isShareable) throws IOException {
        this(BitmapRegionDecoder.newInstance(fd, isShareable));
    }

    public BitmapRegionTileDecoder(final InputStream is, final boolean isShareable) throws IOException {
        this(BitmapRegionDecoder.newInstance(is, isShareable));
    }

    public BitmapRegionTileDecoder(final String pathName, final boolean isShareable) throws IOException {
        this(BitmapRegionDecoder.newInstance(pathName, isShareable));
    }

    @Override
    public void attachToTileView(final TileView tileView) {
        tileView.resetDetailLevels();
        tileView.setTileDecoder(this);
        tileView.setDownsampleDecoder(getDownSampleDecoder());
        final int width = getWidth(), height = getHeight(), tileSize = getTileSize();
        tileView.setSize(width, height);
        for (int i = 0, j = Math.max(width, height) / tileSize; i < j; i++) {
            final int s = i + 1;
            if (isPowerOfTwo(s)) {
                tileView.addDetailLevel(1f / s, BitmapRegionTileDecoder.getDecodeName(s), "sample", tileSize, tileSize);
            }
        }
    }

    @Override
    public Bitmap decode(final String fileName, final Context context) {
        final Matcher m = PATTERN_TILE_FORMAT.matcher(fileName);
        if (!m.matches()) return null;
        final int inSampleSize = Integer.parseInt(m.group(1));
        final int col = Integer.parseInt(m.group(2)), row = Integer.parseInt(m.group(3));
        final int tileSize = inSampleSize * mTileSize;
        final int left = col * tileSize, top = row * tileSize;
        final int right = Math.min(left + tileSize, getWidth());
        final int bottom = Math.min(top + tileSize, getHeight());
        final Rect rect = new Rect(left, top, right, bottom);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return mDecoder.decodeRegion(rect, options);
    }

    public BitmapDecoder getDownSampleDecoder() {
        return mDownSampleDecoder;
    }

    public int getHeight() {
        return mDecoder.getHeight();
    }

    public int getTileSize() {
        return mTileSize;
    }

    public int getWidth() {
        return mDecoder.getWidth();
    }

    @Override
    public boolean isRecycled() {
        return mDecoder.isRecycled();
    }

    @Override
    public void recycle() {
        mDecoder.recycle();
    }

    public boolean isSameDecoder(final BitmapRegionDecoder decoder) {
        return mDecoder.equals(decoder);
    }

    public static String getDecodeName(final int inSampleSize) {
        return String.format("%d:%s-%s", inSampleSize, "%col%", "%row%");
    }

    private static boolean isPowerOfTwo(final int n) {
        return n != 0 && (n & n - 1) == 0;
    }

    private static int nextPowerOfTwo(final int i) {
        int n = i;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n++;
        return n;
    }

    private static final class DownSampleDecoder implements BitmapDecoder {

        private final BitmapRegionDecoder mDecoder;
        private final int mInSampleSize;

        private DownSampleDecoder(final BitmapRegionDecoder decoder, final int inSampleSize) {
            mDecoder = decoder;
            mInSampleSize = inSampleSize;
        }

        @Override
        public Bitmap decode(final String fileName, final Context context) {
            final Rect rect = new Rect(0, 0, mDecoder.getWidth(), mDecoder.getHeight());
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = mInSampleSize;
            return mDecoder.decodeRegion(rect, options);
        }

    }

}