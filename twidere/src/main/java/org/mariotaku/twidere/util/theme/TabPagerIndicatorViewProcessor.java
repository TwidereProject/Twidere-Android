package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.view.TabPagerIndicator;

/**
 * Created by mariotaku on 16/3/15.
 */
public class TabPagerIndicatorViewProcessor implements ViewProcessor<TabPagerIndicator, Object> {
    @Override
    public void process(@NonNull Context context, String key, TabPagerIndicator view, Object extra) {
        final int primaryColor = Config.primaryColor(context, key);
        final boolean isDark = !ATEUtil.isColorLight(primaryColor);
        final int primaryColorDependent = isDark ? Color.WHITE : Color.BLACK;
        view.setIconColor(primaryColorDependent);
        view.setLabelColor(primaryColorDependent);
        if (Config.coloredActionBar(context, key)) {
            view.setStripColor(primaryColorDependent);
        } else {
            view.setStripColor(Config.accentColor(context, key));
        }
        view.updateAppearance();
    }
}
