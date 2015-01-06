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

package org.mariotaku.tileimageview.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.qozix.layouts.ZoomPanLayout.GestureListener;
import com.qozix.layouts.ZoomPanLayout.ZoomPanListener;
import com.qozix.tileview.TileView;
import com.qozix.tileview.TileView.TileViewEventListener;
import com.qozix.tileview.hotspots.HotSpot;
import com.qozix.tileview.hotspots.HotSpotEventListener;
import com.qozix.tileview.markers.MarkerEventListener;

import org.mariotaku.tileimageview.decoder.AbsTileDecoder;
import org.mariotaku.tileimageview.decoder.BitmapRegionTileDecoder;
import org.mariotaku.tileimageview.decoder.DummyTileDecoder;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public final class TileImageView extends FrameLayout {

    private final TileView mTileView;
    private AbsTileDecoder mTileDecoder;

    public TileImageView(final Context context) {
        this(context, null);
    }

    public TileImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mTileView = new TileView(context);
        super.addView(mTileView, 0, generateDefaultLayoutParams());
    }

    public View addCallout(final View view, final double x, final double y) {
        return mTileView.addCallout(view, x, y);
    }

    public View addCallout(final View view, final double x, final double y, final float anchorX, final float anchorY) {
        return mTileView.addCallout(view, x, y, anchorX, anchorY);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void addChildrenForAccessibility(@NonNull final ArrayList<View> childrenForAccessibility) {
        mTileView.addChildrenForAccessibility(childrenForAccessibility);
    }

    @Override
    public void addFocusables(final ArrayList<View> views, final int direction) {
        mTileView.addFocusables(views, direction);
    }

    @Override
    public void addFocusables(@NonNull final ArrayList<View> views, final int direction, final int focusableMode) {
        mTileView.addFocusables(views, direction, focusableMode);
    }

    public boolean addGestureListener(final GestureListener listener) {
        return mTileView.addGestureListener(listener);
    }

    public HotSpot addHotSpot(final HotSpot hotSpot) {
        return mTileView.addHotSpot(hotSpot);
    }

    public HotSpot addHotSpot(final List<double[]> positions) {
        return mTileView.addHotSpot(positions);
    }

    public HotSpot addHotSpot(final List<double[]> positions, final HotSpotEventListener listener) {
        return mTileView.addHotSpot(positions, listener);
    }

    public void addHotSpotEventListener(final HotSpotEventListener listener) {
        mTileView.addHotSpotEventListener(listener);
    }

    public View addMarker(final View view, final double x, final double y) {
        return mTileView.addMarker(view, x, y);
    }

    public View addMarker(final View view, final double x, final double y, final float anchorX, final float anchorY) {
        return mTileView.addMarker(view, x, y, anchorX, anchorY);
    }

    public void addMarkerEventListener(final MarkerEventListener listener) {
        mTileView.addMarkerEventListener(listener);
    }

    public void addTileViewEventListener(final TileViewEventListener listener) {
        mTileView.addTileViewEventListener(listener);
    }

    @Override
    public void addView(@NonNull final View child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addView(@NonNull final View child, final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addView(@NonNull final View child, final int width, final int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addView(@NonNull final View child, final int index, final ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addView(@NonNull final View child, final ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException();
    }

    public boolean addZoomPanListener(final ZoomPanListener listener) {
        return mTileView.addZoomPanListener(listener);
    }

    public void cancelRender() {
        mTileView.cancelRender();
    }

    @Override
    public boolean canScrollHorizontally(final int direction) {
        final double scaledBoundaryX = mTileView.getScaledWidth();
        final int scrollX = mTileView.getScrollX();
        return scrollX + direction > 0 && scrollX + mTileView.getWidth() + direction < scaledBoundaryX;
    }

    @Override
    public boolean canScrollVertically(final int direction) {
        final double scaledBoundaryY = mTileView.getScaledHeight();
        final int scrollY = mTileView.getScrollY();
        return scrollY + direction > 0 && scrollY + mTileView.getHeight() + direction < scaledBoundaryY;
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent ev) {
        return mTileView.dispatchTouchEvent(ev);
    }

    public int getBaseHeight() {
        return mTileView.getBaseHeight();
    }

    public int getBaseWidth() {
        return mTileView.getBaseWidth();
    }

    public Point getCenter() {
        return new Point(mTileView.getScrollX(), mTileView.getScrollY());
    }

    public double getScale() {
        return mTileView.getScale();
    }

    public boolean removeGestureListener(final GestureListener listener) {
        return mTileView.removeGestureListener(listener);
    }

    public void removeHotSpot(final HotSpot hotSpot) {
        mTileView.removeHotSpot(hotSpot);
    }

    public void removeHotSpotEventListener(final HotSpotEventListener listener) {
        mTileView.removeHotSpotEventListener(listener);
    }

    public void removeMarkerEventListener(final MarkerEventListener listener) {
        mTileView.removeMarkerEventListener(listener);
    }

    public void requestRender() {
        mTileView.requestRender();
    }

    public void resume() {
        mTileView.resume();
    }

    public void scrollToAndCenter(final Point point) {
        mTileView.scrollToAndCenter(point);
    }

    public void scrollToPoint(final Point point) {
        mTileView.scrollToPoint(point);
    }

    public void setBitmapRegionDecoder(final BitmapRegionDecoder decoder, final Bitmap fallback) {
        if (mTileDecoder != null && mTileDecoder.isSameDecoder(decoder)) return;
        if (mTileDecoder != null) {
            mTileDecoder.recycle();
            mTileDecoder = null;
        }
        if (decoder == null) {
            mTileDecoder = new DummyTileDecoder(fallback);
        } else {
            mTileDecoder = new BitmapRegionTileDecoder(decoder);
        }
        mTileDecoder.attachToTileView(mTileView);
    }

    public void setDragStartThreshold(final int threshold) {
        mTileView.setDragStartThreshold(threshold);
    }

    public void setScale(final double d) {
        mTileView.setScale(d);
    }

    public void setScaleLimits(final double min, final double max) {
        mTileView.setScaleLimits(min, max);
    }

    public void setScaleToFit(final boolean shouldScaleToFit) {
        mTileView.setScaleToFit(shouldScaleToFit);
    }

    public void slideToAndCenter(final double x, final double y) {
        mTileView.slideToAndCenter(x, y);
    }

    public void slideToAndCenter(final Point point) {
        mTileView.slideToAndCenter(point);
    }

    public void slideToPoint(final Point point) {
        mTileView.slideToPoint(point);
    }

    public void smoothScaleTo(final double destination, final int duration) {
        mTileView.smoothScaleTo(destination, duration);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
    }

}