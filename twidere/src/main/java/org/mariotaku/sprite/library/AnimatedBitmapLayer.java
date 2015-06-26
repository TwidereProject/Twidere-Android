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

package org.mariotaku.sprite.library;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.view.Gravity;

/**
 * Created by mariotaku on 15/6/26.
 */
public class AnimatedBitmapLayer implements Layer {

    private final Bitmap mBitmap;
    private final int mFrames;
    private final boolean mVerticalFrames;
    private final Point mPosition = new Point();
    private final Point mFrameSize = new Point(), mScaledSize = new Point();
    private final Paint mPaint = new Paint();
    private Rect mSource = new Rect(), mDestination = new Rect(), mDisplayBounds = new Rect();
    private Rect mTempDestination = new Rect();

    private int mGravity;
    private int mCurrentFrame;
    private Shader.TileMode mTileModeX, mTileModeY;

    public AnimatedBitmapLayer(Resources resources, int bitmapRes, int frames, boolean verticalFrames) {
        this(BitmapFactory.decodeResource(resources, bitmapRes), frames, verticalFrames);
    }

    public AnimatedBitmapLayer(Bitmap bitmap, int frames, boolean verticalFrames) {
        mBitmap = bitmap;
        mFrames = frames;
        mVerticalFrames = verticalFrames;
        final int bitmapWidth = bitmap.getWidth(), bitmapHeight = bitmap.getHeight();
        mFrameSize.x = verticalFrames ? bitmapWidth : bitmapWidth / frames;
        mFrameSize.y = verticalFrames ? bitmapHeight / frames : bitmapHeight;

        setGravity(Gravity.NO_GRAVITY);
        setAntiAlias(true);
        setScale(1);
    }

    @Override
    public void onDraw(final Canvas canvas) {
        final int frame = mCurrentFrame++;
        if (mVerticalFrames) {
            final int top = mFrameSize.y * frame;
            mSource.set(0, top, mFrameSize.x, top + mFrameSize.y);
        } else {
            final int left = mFrameSize.x * frame;
            mSource.set(left, 0, left + mFrameSize.x, mFrameSize.y);
        }

        final int destWidth = mTileModeX == Shader.TileMode.REPEAT ? mScaledSize.x : mDestination.width();
        final int destHeight = mTileModeY == Shader.TileMode.REPEAT ? mScaledSize.y : mDestination.height();
        for (int l = mDestination.left, r = mDestination.right; l < r; l += destWidth) {
            for (int t = mDestination.top, b = mDestination.bottom; t < b; t += destHeight) {
                mTempDestination.left = l;
                mTempDestination.right = l + destWidth;
                mTempDestination.top = t;
                mTempDestination.bottom = t + destHeight;
                canvas.drawBitmap(mBitmap, mSource, mTempDestination, mPaint);
            }
        }
        if (mCurrentFrame >= mFrames) {
            mCurrentFrame = 0;
        }
    }

    @Override
    public void onSizeChanged(final int width, final int height) {
        mDisplayBounds.set(0, 0, width, height);
        updateDestination();
    }

    public void setGravity(int gravity) {
        mGravity = gravity;
        updateDestination();
    }

    public void setAntiAlias(boolean aa) {
        mPaint.setAntiAlias(aa);
    }


    public void setTileMode(Shader.TileMode tileModeX, Shader.TileMode tileModeY) {
        mTileModeX = tileModeX;
        mTileModeY = tileModeY;
    }

    /**
     * Set position relative to gravity
     */
    public void setPosition(int x, int y) {
        mPosition.set(x, y);
        updateDestination();
    }

    public void setScale(int scale) {
        mScaledSize.x = mFrameSize.x * scale;
        mScaledSize.y = mFrameSize.y * scale;
        updateDestination();
    }

    private void updateDestination() {
        Gravity.apply(mGravity, mScaledSize.x, mScaledSize.y, mDisplayBounds, mPosition.x, mPosition.y, mDestination);
    }
}
