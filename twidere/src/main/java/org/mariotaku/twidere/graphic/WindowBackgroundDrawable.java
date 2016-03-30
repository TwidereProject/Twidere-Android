package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by mariotaku on 16/3/30.
 */
public class WindowBackgroundDrawable extends Drawable {

    private final int color;

    public WindowBackgroundDrawable(int color) {
        this.color = color;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(color);
    }

    @Override
    public void setAlpha(int alpha) {
        // No-op
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // No-op
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
