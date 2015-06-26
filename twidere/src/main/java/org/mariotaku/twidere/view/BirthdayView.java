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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;

import org.mariotaku.sprite.library.AnimatedBitmapLayer;
import org.mariotaku.sprite.library.Layer;
import org.mariotaku.sprite.library.LayeredCanvasView;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 15/6/26.
 */
public final class BirthdayView extends LayeredCanvasView {
    public BirthdayView(final Context context) {
        super(context);
        init();
    }

    public BirthdayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BirthdayView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(0xFF203040);
        final AnimatedBitmapLayer tableLayer = new AnimatedBitmapLayer(getResources(), R.drawable.sprite_birthday_table_frames, 4, true);
        final AnimatedBitmapLayer cakeLayer = new AnimatedBitmapLayer(getResources(), R.drawable.sprite_birthday_cake_frames, 4, false);
        final AnimatedBitmapLayer lightStripLayer = new AnimatedBitmapLayer(getResources(), R.drawable.sprite_birthday_light_strip_frames, 4, true);
        tableLayer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        tableLayer.setAntiAlias(false);
        cakeLayer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cakeLayer.setAntiAlias(false);
        lightStripLayer.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL);
        lightStripLayer.setAntiAlias(false);
        lightStripLayer.setTileMode(Shader.TileMode.REPEAT, null);
        super.setLayers(new Layer[]{tableLayer, cakeLayer, lightStripLayer}, 1);
    }

    @Override
    public void setLayers(final Layer[] layers, final int fps) {
        // No-op
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final Layer[] layers = getLayers();
        ((AnimatedBitmapLayer) layers[0]).setScale(Math.max(1, w / 160));
        ((AnimatedBitmapLayer) layers[1]).setScale(Math.max(1, w / 160));
        ((AnimatedBitmapLayer) layers[2]).setScale(Math.max(1, w / 160));
    }

    @Override
    protected boolean fitSystemWindows(@NonNull Rect insets) {
        final int stripTop = Utils.getInsetsTopWithoutActionBarHeight(getContext(), insets.top);
        final Layer[] layers = getLayers();
        ((AnimatedBitmapLayer) layers[2]).setPosition(0, stripTop);
        return super.fitSystemWindows(insets);
    }
}
