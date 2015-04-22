package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;

public class StatusTextView extends AppCompatTextView {

    public StatusTextView(final Context context) {
        this(context, null);
    }

    public StatusTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public StatusTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setEditableFactory(new SafeEditableFactory());
        setSpannableFactory(new SafeSpannableFactory());
    }

    private static class SafeSpannableString extends SpannableString {

        public SafeSpannableString(CharSequence source) {
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

    private static class SafeSpannableStringBuilder extends SpannableStringBuilder {

        public SafeSpannableStringBuilder(CharSequence source) {
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
