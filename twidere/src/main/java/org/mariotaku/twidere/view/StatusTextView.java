package org.mariotaku.twidere.view;

import android.content.Context;
import android.text.SpannableString;
import android.util.AttributeSet;

import org.mariotaku.twidere.view.themed.ThemedTextView;

public class StatusTextView extends ThemedTextView {

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

    public void setOnSelectionChangeListener(final OnSelectionChangeListener l) {
        mOnSelectionChangeListener = l;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            super.setText(null, type);
            return;
        }
        super.setText(new SafeSpannableStringWrapper(text), type);
    }

    @Override
    protected void onSelectionChanged(final int selStart, final int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mOnSelectionChangeListener != null) {
            mOnSelectionChangeListener.onSelectionChanged(selStart, selEnd);
        }
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selStart, int selEnd);
    }

    private static class SafeSpannableStringWrapper extends SpannableString {

        public SafeSpannableStringWrapper(CharSequence source) {
            super(source);
        }

        @Override
        public void setSpan(Object what, int start, int end, int flags) {
            if (start < 0 || end < 0) {
                // Silently ignore
                return;
            }
            super.setSpan(what, start, end, flags);
        }
    }

}
