package org.mariotaku.twidere.preference;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 14-7-28.
 */
public class ForegroundColorIconPreference extends Preference {
    public ForegroundColorIconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        final Drawable icon = getIcon();
        if (icon != null) {
            icon.setColorFilter(ThemeUtils.getThemeForegroundColor(getContext()), Mode.SRC_ATOP);
        }
        setIcon(icon);
    }

    public ForegroundColorIconPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public ForegroundColorIconPreference(Context context) {
        this(context, null);
    }
}
