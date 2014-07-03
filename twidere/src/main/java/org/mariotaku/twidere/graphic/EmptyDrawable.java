package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class EmptyDrawable extends Drawable {

	@Override
	public void draw(final Canvas canvas) {

	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setAlpha(final int alpha) {

	}

	@Override
	public void setColorFilter(final ColorFilter cf) {

	}

}
