package org.mariotaku.twidere.view;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.text.SafeSpannableString;
import org.mariotaku.twidere.text.SafeSpannableStringBuilder;
import org.mariotaku.twidere.view.themed.ThemedTextView;

public class StatusTextView extends ThemedTextView {

    public StatusTextView(final Context context) {
        super(context);
        init();
    }

    public StatusTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        // Android clears TextView when setText(), so setText before touch
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            final CharSequence text = getText();
            setText(null);
            setText(text);
        }
        return super.dispatchTouchEvent(event);
    }

    private void init() {
        setEditableFactory(new SafeEditableFactory());
        setSpannableFactory(new SafeSpannableFactory());
    }

    private class SafeEditableFactory extends Editable.Factory {
        @Override
        public Editable newEditable(CharSequence source) {
            return new SafeSpannableStringBuilder(source);
        }
    }

    private class SafeSpannableFactory extends Spannable.Factory {
        @Override
        public Spannable newSpannable(CharSequence source) {
            return new SafeSpannableString(source);
        }
    }
}
