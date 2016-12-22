package org.mariotaku.chameleon;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by mariotaku on 2016/12/18.
 */

public interface ChameleonView {

    boolean isPostApplyTheme();

    @Nullable
    Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme);

    void applyAppearance(@NonNull Appearance appearance);

    interface Appearance {

    }

    interface StatusBarThemeable {
        boolean isStatusBarColorHandled();
    }
}
