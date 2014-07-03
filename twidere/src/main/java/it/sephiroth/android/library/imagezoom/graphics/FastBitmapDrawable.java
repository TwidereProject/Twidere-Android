package it.sephiroth.android.library.imagezoom.graphics;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import java.io.InputStream;

/**
 * Fast bitmap drawable. Does not support states. it only support alpha and
 * colormatrix
 * 
 * @author alessandro
 */
public class FastBitmapDrawable extends Drawable implements IBitmapDrawable {

	protected Bitmap mBitmap;
	protected Paint mPaint;

	public FastBitmapDrawable(final Bitmap b) {
		mBitmap = b;
		mPaint = new Paint();
		mPaint.setDither(true);
		mPaint.setFilterBitmap(true);
	}

	public FastBitmapDrawable(final Resources res, final InputStream is) {
		this(BitmapFactory.decodeStream(is));
	}

	@Override
	public void draw(final Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint);
	}

	@Override
	public Bitmap getBitmap() {
		return mBitmap;
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmap.getHeight();
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getMinimumHeight() {
		return mBitmap.getHeight();
	}

	@Override
	public int getMinimumWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(final int alpha) {
		mPaint.setAlpha(alpha);
	}

	public void setAntiAlias(final boolean value) {
		mPaint.setAntiAlias(value);
		invalidateSelf();
	}

	@Override
	public void setColorFilter(final ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}
}
