package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 16/3/16.
 */
public class FloatingActionButtonViewProcessor implements ViewProcessor<FloatingActionButton, Object> {
    @Override
    public void process(@NonNull Context context, String key, FloatingActionButton target, Object extra) {
        final int primaryColor = Config.primaryColor(context, key);
        final int primaryColorDependent = ThemeUtils.getColorDependent(primaryColor);
        target.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        target.setColorFilter(primaryColorDependent);
    }

}
