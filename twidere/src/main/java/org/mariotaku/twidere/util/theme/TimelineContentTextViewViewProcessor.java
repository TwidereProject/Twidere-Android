package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.TimelineContentTextView;

/**
 * Created by mariotaku on 16/3/18.
 */
public class TimelineContentTextViewViewProcessor implements ViewProcessor<TimelineContentTextView, Object> {
    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable TimelineContentTextView target, @Nullable Object extra) {
        if (target == null) return;
        final int accentColor = Config.accentColor(context, key);
        target.setLinkTextColor(ThemeUtils.getOptimalAccentColor(accentColor, target.getCurrentTextColor()));
    }
}
