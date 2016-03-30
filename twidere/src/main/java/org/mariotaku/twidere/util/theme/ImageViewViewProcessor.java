package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 16/3/30.
 */
public class ImageViewViewProcessor implements ViewProcessor<ImageView, Void> {
    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable ImageView target, @Nullable Void extra) {
        if (target == null) return;
        switch (target.getId()) {
            case android.support.v7.appcompat.R.id.action_mode_close_button: {
                target.setColorFilter(ThemeUtils.getColorDependent(Config.toolbarColor(context, key, null)));
                break;
            }
        }
    }
}
