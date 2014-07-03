/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

// This class aggregates three gesture detectors: GestureDetector,
// ScaleGestureDetector, and DownUpDetector.
@TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
public class GestureRecognizer {
	@SuppressWarnings("unused")
	private static final String TAG = "GestureRecognizer";

	private final GestureDetector mGestureDetector;

	private final ScaleGestureDetector mScaleDetector;
	private final DownUpDetector mDownUpDetector;
	private final Listener mListener;

	public GestureRecognizer(final Context context, final Listener listener) {
		mListener = listener;
		mGestureDetector = new GestureDetector(context, new MyGestureListener(), null, true /* ignoreMultitouch */);
		mScaleDetector = new ScaleGestureDetector(context, new MyScaleListener());
		mDownUpDetector = new DownUpDetector(new MyDownUpListener());
	}

	public void cancelScale() {
		final long now = SystemClock.uptimeMillis();
		final MotionEvent cancelEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0, 0, 0);
		mScaleDetector.onTouchEvent(cancelEvent);
		cancelEvent.recycle();
	}

	public boolean isDown() {
		return mDownUpDetector.isDown();
	}

	public void onTouchEvent(final MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		mScaleDetector.onTouchEvent(event);
		mDownUpDetector.onTouchEvent(event);
	}

	public interface Listener {
		boolean onDoubleTap(float x, float y);

		void onDown(float x, float y);

		boolean onFling(float velocityX, float velocityY);

		boolean onScale(float focusX, float focusY, float scale);

		boolean onScaleBegin(float focusX, float focusY);

		void onScaleEnd();

		boolean onScroll(float dx, float dy, float totalX, float totalY);

		boolean onSingleTapUp(float x, float y);

		void onUp();
	}

	private class MyDownUpListener implements DownUpDetector.DownUpListener {
		@Override
		public void onDown(final MotionEvent e) {
			mListener.onDown(e.getX(), e.getY());
		}

		@Override
		public void onUp(final MotionEvent e) {
			mListener.onUp();
		}
	}

	private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
		@Override
		public boolean onDoubleTap(final MotionEvent e) {
			return mListener.onDoubleTap(e.getX(), e.getY());
		}

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
			return mListener.onFling(velocityX, velocityY);
		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float dx, final float dy) {
			return mListener.onScroll(dx, dy, e2.getX() - e1.getX(), e2.getY() - e1.getY());
		}

		@Override
		public boolean onSingleTapUp(final MotionEvent e) {
			return mListener.onSingleTapUp(e.getX(), e.getY());
		}
	}

	private class MyScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(final ScaleGestureDetector detector) {
			return mListener.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
		}

		@Override
		public boolean onScaleBegin(final ScaleGestureDetector detector) {
			return mListener.onScaleBegin(detector.getFocusX(), detector.getFocusY());
		}

		@Override
		public void onScaleEnd(final ScaleGestureDetector detector) {
			mListener.onScaleEnd();
		}
	}
}
