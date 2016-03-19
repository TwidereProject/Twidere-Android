package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.appthemeengine.tagprocessors.TagProcessor;

import org.mariotaku.twidere.view.iface.IIconActionButton;

/**
 * Created by mariotaku on 16/3/19.
 */
public class IconActionButtonTagProcessor extends TagProcessor {

    public static final String PREFIX_COLOR = "iab_color";
    public static final String PREFIX_COLOR_ACTIVATED = "iab_activated_color";
    public static final String PREFIX_COLOR_DISABLED = "iab_disabled_color";

    @NonNull
    private final String mPrefix;

    public IconActionButtonTagProcessor(@NonNull String prefix) {
        mPrefix = prefix;
    }

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return view instanceof IIconActionButton;
    }

    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
        final IIconActionButton iab = (IIconActionButton) view;
        final ColorResult colorResult = getColorFromSuffix(context, key, view, suffix);
        if (colorResult == null) return;
        switch (mPrefix) {
            case PREFIX_COLOR: {
                iab.setDefaultColor(colorResult.getColor());
                break;
            }
            case PREFIX_COLOR_ACTIVATED: {
                iab.setActivatedColor(colorResult.getColor());
                break;
            }
            case PREFIX_COLOR_DISABLED: {
                iab.setDisabledColor(colorResult.getColor());
                break;
            }
        }
    }
}
