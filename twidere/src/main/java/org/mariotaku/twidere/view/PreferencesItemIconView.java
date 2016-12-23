package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;

/**
 * Created by mariotaku on 2016/12/23.
 */

public class PreferencesItemIconView extends IconActionView {
    public PreferencesItemIconView(Context context) {
        super(context);
    }

    public PreferencesItemIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferencesItemIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        appearance.setActivatedColor(ChameleonUtils.getColorDependent(theme.getColorControlActivated()));
        appearance.setDefaultColor(theme.getColorForeground());
        return appearance;
    }
}
