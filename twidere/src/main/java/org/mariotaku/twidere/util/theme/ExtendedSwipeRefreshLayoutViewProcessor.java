package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.view.ExtendedSwipeRefreshLayout;

/**
 * Created by mariotaku on 16/3/18.
 */
public class ExtendedSwipeRefreshLayoutViewProcessor implements ViewProcessor<ExtendedSwipeRefreshLayout, Object> {
    @Override
    public void process(@NonNull Context context, String key, ExtendedSwipeRefreshLayout target, Object extra) {
        target.setColorSchemeColors(Config.accentColor(context, key));
    }
}
