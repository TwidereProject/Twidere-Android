package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by mariotaku on 16/3/18.
 */
public class ProgressWheelViewProcessor implements ViewProcessor<ProgressWheel, Object> {
    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable ProgressWheel target, @Nullable Object extra) {
        if (target == null) return;
        target.setBarColor(Config.accentColor(context, key));
    }
}
