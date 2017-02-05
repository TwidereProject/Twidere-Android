package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.jetbrains.annotations.NotNull;
import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;

/**
 * Created by mariotaku on 2016/12/23.
 */

public class PreferencesItemTextView extends FixedTextView {

    private static final int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    private static final int[] EMPTY_STATE_SET = {0};

    public PreferencesItemTextView(@NotNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        final int activatedColor = ChameleonUtils.getColorDependent(theme.getColorControlActivated());
        final int defaultColor = theme.getTextColorPrimary();
        appearance.setTextColor(new ColorStateList(new int[][]{ACTIVATED_STATE_SET, EMPTY_STATE_SET}, new int[]{activatedColor, defaultColor}));
        return appearance;
    }
}
