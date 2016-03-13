package org.mariotaku.twidere.preference;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 14-7-28.
 */
public class ForegroundColorIconPreference extends Preference {
    public ForegroundColorIconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        final int fgColor = ThemeUtils.getThemeForegroundColor(getContext());
        ((ImageView) view.findViewById(android.R.id.icon)).setColorFilter(fgColor, Mode.SRC_ATOP);
    }

    public ForegroundColorIconPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public ForegroundColorIconPreference(Context context) {
        this(context, null);
    }
}
