package org.mariotaku.twidere.view.iface;

import android.support.annotation.ColorInt;

/**
 * Created by mariotaku on 16/3/19.
 */
public interface IIconActionButton {
    @ColorInt
    int getDefaultColor();

    @ColorInt
    int getActivatedColor();

    @ColorInt
    int getDisabledColor();

    void setDefaultColor(@ColorInt int defaultColor);

    void setActivatedColor(@ColorInt int activatedColor);

    void setDisabledColor(@ColorInt int disabledColor);
}
