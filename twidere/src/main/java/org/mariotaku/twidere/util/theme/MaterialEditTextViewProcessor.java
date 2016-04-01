package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by mariotaku on 16/4/1.
 */
public class MaterialEditTextViewProcessor implements ViewProcessor<MaterialEditText, Void> {
    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable MaterialEditText target, @Nullable Void extra) {
        if (target == null) return;
        int accentColor = Config.accentColor(context, key);
        target.setPrimaryColor(accentColor);
    }
}
