package org.mariotaku.twidere.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.twidere.util.EmojiSupportUtils;
import org.mariotaku.twidere.view.themed.ThemedTextView;

public class StatusTextView extends ThemedTextView {

    public StatusTextView(final Context context) {
        super(context);
        EmojiSupportUtils.initForTextView(this);
    }

    public StatusTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        EmojiSupportUtils.initForTextView(this);
    }

    public StatusTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        EmojiSupportUtils.initForTextView(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        // Android clears TextView when setText(), so setText before touch
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (getSelectionEnd() != getSelectionStart()) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return LinkMovementMethod.getInstance();
    }

}