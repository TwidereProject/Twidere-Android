package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.tagprocessors.TagProcessor;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 16/3/18.
 */
public class OptimalLinkColorTagProcessor extends TagProcessor {
    public static final String TAG = "optimal_link_color";

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return view instanceof TextView;
    }

    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
        TextView tv = (TextView) view;
        final int accentColor = Config.accentColor(context, key);
        tv.setLinkTextColor(ThemeUtils.getOptimalAccentColor(accentColor, tv.getCurrentTextColor()));
    }
}
