package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.inflation.ViewInterface;
import com.afollestad.appthemeengine.tagprocessors.TagProcessor;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.appthemeengine.util.TintHelper;

import org.mariotaku.multivalueswitch.library.MultiValueSwitch;

public class ThemedMultiValueSwitch extends MultiValueSwitch implements ViewInterface {

    public static final String PREFIX_TINT = "mvs_tint";

    public ThemedMultiValueSwitch(Context context) {
        super(context);
    }

    public ThemedMultiValueSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public static void setTint(@NonNull MultiValueSwitch switchView, @ColorInt int color, boolean useDarker) {
        if (switchView.getTrackDrawable() != null) {
            switchView.setTrackDrawable(modifySwitchDrawable(switchView.getContext(),
                    switchView.getTrackDrawable(), color, false, true, useDarker));
        }
        if (switchView.getThumbDrawable() != null) {
            switchView.setThumbDrawable(modifySwitchDrawable(switchView.getContext(),
                    switchView.getThumbDrawable(), color, true, true, useDarker));
        }
    }


    private static Drawable modifySwitchDrawable(@NonNull Context context, @NonNull Drawable from, @ColorInt int tint, boolean thumb, boolean compatSwitch, boolean useDarker) {
        if (useDarker) {
            tint = ATEUtil.shiftColor(tint, 1.1f);
        }
        tint = ATEUtil.adjustAlpha(tint, (compatSwitch && !thumb) ? 0.5f : 1.0f);
        int disabled;
        int normal;
        if (thumb) {
            disabled = ContextCompat.getColor(context, useDarker ? com.afollestad.appthemeengine.R.color.ate_switch_thumb_disabled_dark : com.afollestad.appthemeengine.R.color.ate_switch_thumb_disabled_light);
            normal = ContextCompat.getColor(context, useDarker ? com.afollestad.appthemeengine.R.color.ate_switch_thumb_normal_dark : com.afollestad.appthemeengine.R.color.ate_switch_thumb_normal_light);
        } else {
            disabled = ContextCompat.getColor(context, useDarker ? com.afollestad.appthemeengine.R.color.ate_switch_track_disabled_dark : com.afollestad.appthemeengine.R.color.ate_switch_track_disabled_light);
            normal = ContextCompat.getColor(context, useDarker ? com.afollestad.appthemeengine.R.color.ate_switch_track_normal_dark : com.afollestad.appthemeengine.R.color.ate_switch_track_normal_light);
        }

        // Stock switch includes its own alpha
        if (!compatSwitch) {
            normal = ATEUtil.stripAlpha(normal);
        }

        final ColorStateList sl = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_enabled, -android.R.attr.state_activated, -android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_activated},
                        new int[]{android.R.attr.state_enabled, android.R.attr.state_checked}
                },
                new int[]{
                        disabled,
                        normal,
                        tint,
                        tint
                }
        );
        return TintHelper.createTintedDrawable(from, sl);
    }


    public void setKey(String key) {
        ATE.themeView(getContext(), this, key);
    }

    @Override
    public boolean isShown() {
        return getParent() != null && getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean setsStatusBarColor() {
        return false;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }

    public static class TintTagProcessor extends TagProcessor {
        @Override
        public boolean isTypeSupported(@NonNull View view) {
            return view instanceof MultiValueSwitch;
        }

        @Override
        public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
            final ColorResult result = getColorFromSuffix(context, key, view, suffix);
            if (result == null) return;

            setTint((MultiValueSwitch) view, result.getColor(), result.isDark(context));

        }
    }
}
