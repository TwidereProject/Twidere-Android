package org.mariotaku.twidere.util.support;

import android.text.SpannableStringBuilder;

/**
 * Created by mariotaku on 16/4/4.
 */
public class SpannableStringBuilderSupport {

    private SpannableStringBuilderSupport() {
    }

    public static void append(SpannableStringBuilder builder, CharSequence text, Object span, int flags) {
        int start = builder.length();
        builder.append(text);
        builder.setSpan(span, start, builder.length(), flags);
    }

}
