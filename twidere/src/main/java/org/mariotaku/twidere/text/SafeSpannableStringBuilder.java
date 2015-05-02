package org.mariotaku.twidere.text;

import android.text.SpannableStringBuilder;

/**
 * Created by Ningyuan on 2015/5/1.
 */
public class SafeSpannableStringBuilder extends SpannableStringBuilder {

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
