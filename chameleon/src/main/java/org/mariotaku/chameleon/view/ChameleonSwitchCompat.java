package org.mariotaku.chameleon.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;

/**
 * Created by mariotaku on 2016/12/21.
 */

public class ChameleonSwitchCompat extends SwitchCompat implements ChameleonView {

    public ChameleonSwitchCompat(Context context) {
        super(context);
    }

    public ChameleonSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static void setTint(@NonNull SwitchCompat switchView, @ColorInt int color, boolean useDarker) {
        if (switchView.getTrackDrawable() != null) {
            switchView.setTrackDrawable(modifySwitchDrawable(switchView.getContext(),
                    switchView.getTrackDrawable(), color, false, true, useDarker));
        }
        if (switchView.getThumbDrawable() != null) {
            switchView.setThumbDrawable(modifySwitchDrawable(switchView.getContext(),
                    switchView.getThumbDrawable(), color, true, true, useDarker));
        }
    }


    public static Drawable modifySwitchDrawable(@NonNull Context context, @NonNull Drawable from,
                                                @ColorInt int tint, boolean thumb, boolean compatSwitch,
                                                boolean useDarker) {
        if (useDarker) {
            tint = ChameleonUtils.shiftColor(tint, 1.1f);
        }
        tint = ChameleonUtils.adjustAlpha(tint, (compatSwitch && !thumb) ? 0.5f : 1.0f);
        int disabled;
        int normal;
        if (thumb) {
            disabled = ContextCompat.getColor(context, useDarker ? R.color.chameleon_switch_thumb_disabled_dark : R.color.chameleon_switch_thumb_disabled_light);
            normal = ContextCompat.getColor(context, useDarker ? R.color.chameleon_switch_thumb_normal_dark : R.color.chameleon_switch_thumb_normal_light);
        } else {
            disabled = ContextCompat.getColor(context, useDarker ? R.color.chameleon_switch_track_disabled_dark : R.color.chameleon_switch_track_disabled_light);
            normal = ContextCompat.getColor(context, useDarker ? R.color.chameleon_switch_track_normal_dark : R.color.chameleon_switch_track_normal_light);
        }

        // Stock switch includes its own alpha
        if (!compatSwitch) {
            normal = ChameleonUtils.stripAlpha(normal);
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
        return ChameleonUtils.createTintedDrawable(from, sl);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        return Appearance.create(theme);
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setTint(this, a.getAccentColor(), a.isDark());
    }

    public static class Appearance implements ChameleonView.Appearance {
        int accentColor;
        boolean dark;

        @NonNull
        public static Appearance create(@NonNull Chameleon.Theme theme) {
            Appearance appearance = new Appearance();
            appearance.setAccentColor(theme.getColorAccent());
            appearance.setDark(!ChameleonUtils.isColorLight(theme.getColorBackground()));
            return appearance;
        }

        public int getAccentColor() {
            return accentColor;
        }

        public void setAccentColor(int accentColor) {
            this.accentColor = accentColor;
        }

        public boolean isDark() {
            return dark;
        }

        public void setDark(boolean dark) {
            this.dark = dark;
        }
    }
}
