package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.view.iface.IExtendedView;
import org.mariotaku.twidere.view.themed.ThemedTextView;

public class StatusTextView extends ThemedTextView implements IExtendedView {

	private TouchInterceptor mTouchInterceptor;
	private OnSizeChangedListener mOnSizeChangedListener;
	private OnSelectionChangeListener mOnSelectionChangeListener;

	public StatusTextView(final Context context) {
		super(context);
	}

	public StatusTextView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public StatusTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public final boolean dispatchTouchEvent(final MotionEvent event) {
		if (mTouchInterceptor != null) {
			final boolean ret = mTouchInterceptor.dispatchTouchEvent(this, event);
			if (ret) return true;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		if (mTouchInterceptor != null) {
			final boolean ret = mTouchInterceptor.onTouchEvent(this, event);
			if (ret) return true;
		}
		return super.onTouchEvent(event);
	}

	public void setOnSelectionChangeListener(final OnSelectionChangeListener l) {
		mOnSelectionChangeListener = l;
	}

	@Override
	public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	@Override
	public final void setTouchInterceptor(final TouchInterceptor listener) {
		mTouchInterceptor = listener;
	}

	@Override
	protected void onSelectionChanged(final int selStart, final int selEnd) {
		super.onSelectionChanged(selStart, selEnd);
		if (mOnSelectionChangeListener != null) {
			mOnSelectionChangeListener.onSelectionChanged(selStart, selEnd);
		}
	}

	@Override
	protected final void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}

	public interface OnSelectionChangeListener {
		void onSelectionChanged(int selStart, int selEnd);
	}

}
