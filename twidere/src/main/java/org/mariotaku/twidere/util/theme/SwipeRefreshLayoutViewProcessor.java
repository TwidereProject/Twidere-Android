package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

/**
 * Created by mariotaku on 16/3/18.
 */
public class SwipeRefreshLayoutViewProcessor implements ViewProcessor<SwipeRefreshLayout, Object> {
    @Override
    public void process(@NonNull Context context, String key, SwipeRefreshLayout target, Object extra) {
        target.setColorSchemeColors(Config.accentColor(context, key));
    }
}
