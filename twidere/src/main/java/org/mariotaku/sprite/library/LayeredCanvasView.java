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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by mariotaku on 15/6/26.
 */
public class LayeredCanvasView extends View {
    private Layer[] mLayers;
    private Runnable mAnimateCallback;

    public LayeredCanvasView(final Context context) {
        super(context);
    }

    public LayeredCanvasView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public LayeredCanvasView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLayers(Layer[] layers, int fps) {
        mLayers = layers;
        if (layers == null || fps <= 0) {
            removeCallbacks(mAnimateCallback);
            return;
        }
        notifySizeChanged();
        final long delay = 1000 / fps;
        post(mAnimateCallback = new Runnable() {
            @Override
            public void run() {
                invalidate();
                postDelayed(this, delay);
            }
        });
    }

    public Layer[] getLayers() {
        return mLayers;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        if (mLayers == null) return;
        for (Layer layer : mLayers) {
            layer.onDraw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        notifySizeChanged();
    }

    private void notifySizeChanged() {
        if (mLayers == null) return;
        final int width = getWidth(), height = getHeight();
        for (Layer layer : mLayers) {
            layer.onSizeChanged(width, height);
        }
    }
}
