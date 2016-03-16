package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

/**
 * Created by mariotaku on 16/3/16.
 */
public class FloatingActionButtonViewProcessor implements ViewProcessor<FloatingActionButton, Object> {
    @Override
    public void process(@NonNull Context context, String key, FloatingActionButton target, Object extra) {
        final int primaryColor = Config.primaryColor(context, key);
        final boolean isDark = !ATEUtil.isColorLight(primaryColor);
        final int primaryColorDependent = isDark ? Color.WHITE : Color.BLACK;
        target.setBackgroundTintList(ColorStateList.valueOf(primaryColor));
        target.setColorFilter(primaryColorDependent);
    }
}
