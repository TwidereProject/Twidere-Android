package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;

import org.mariotaku.twidere.text.SafeSpannableString;
import org.mariotaku.twidere.text.SafeSpannableStringBuilder;

public class StatusTextView extends HandleSpanClickTextView {

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
