package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14-7-28.
 */
public class ResourceIconPreference extends Preference {
    public ResourceIconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ResourceIconPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public ResourceIconPreference(Context context) {
        this(context, null);
    }
}
