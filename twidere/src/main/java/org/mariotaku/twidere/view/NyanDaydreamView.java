/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import org.mariotaku.twidere.util.NyanDrawingHelper;

public class NyanDaydreamView extends View {

	private final InvalidateRunnable mInvalidateRunnable;

	private final NyanDrawingHelper mNyanDrawingHelper;

	public NyanDaydreamView(final Context context) {
		this(context, null);
	}

	public NyanDaydreamView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NyanDaydreamView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		mNyanDrawingHelper = new DreamViewNyanDrawingHelper(this);
		mInvalidateRunnable = new InvalidateRunnable(this);
	}

	public void setScale(final float scale) {
		mNyanDrawingHelper.setScale(scale);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		post(mInvalidateRunnable);
	}

	@Override
	protected void onDetachedFromWindow() {
		removeCallbacks(mInvalidateRunnable);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		mNyanDrawingHelper.dispatchDraw(canvas);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mNyanDrawingHelper.dispatchSizeChanged(w, h);
	}

	private static final class DreamViewNyanDrawingHelper extends NyanDrawingHelper {

		private final int mDisplayHeight;
		private final NyanDaydreamView mView;

		public DreamViewNyanDrawingHelper(final NyanDaydreamView view) {
			super(view.getContext());
			mView = view;
			final Resources res = getResources();
			final DisplayMetrics dm = res.getDisplayMetrics();
			mDisplayHeight = dm.heightPixels;
		}

		@Override
		protected int getRainbowYOffset() {
			final int visibility = mView.getSystemUiVisibility();
			if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) return 0;
			return mDisplayHeight - getHeight();
		}

	}

	private static final class InvalidateRunnable implements Runnable {

		private final View mView;

		InvalidateRunnable(final View view) {
			mView = view;
		}

		@Override
		public void run() {
			mView.invalidate();
			mView.postDelayed(this, 66);
		}

	}

}
