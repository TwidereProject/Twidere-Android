package org.mariotaku.twidere.preference;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by mariotaku on 16/3/22.
 */
public class EntrySummaryListPreference extends ThemedListPreference {
    public EntrySummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public EntrySummaryListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EntrySummaryListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EntrySummaryListPreference(Context context) {
        super(context);
    }

    @Override
    public CharSequence getSummary() {
        return getEntry();
    }
}
