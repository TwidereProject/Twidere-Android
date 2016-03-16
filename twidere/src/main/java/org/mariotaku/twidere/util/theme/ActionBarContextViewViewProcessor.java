package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.ActionBarContextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

/**
 * Created by mariotaku on 16/3/16.
 */
public class ActionBarContextViewViewProcessor implements ViewProcessor<ActionBarContextView, Object> {
    @Override
    public void process(@NonNull Context context, String key, ActionBarContextView target,
                        Object extra) {
        final int primaryColor = Config.primaryColor(context, key);
        target.setBackgroundColor(primaryColor);
    }
}
