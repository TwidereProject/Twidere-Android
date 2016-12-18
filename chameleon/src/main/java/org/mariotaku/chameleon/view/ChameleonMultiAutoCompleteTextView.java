package org.mariotaku.chameleon.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonView;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonMultiAutoCompleteTextView extends AppCompatMultiAutoCompleteTextView implements ChameleonView {
    public ChameleonMultiAutoCompleteTextView(Context context) {
        super(context);
    }

    public ChameleonMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public ChameleonEditText.Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        return ChameleonEditText.Appearance.create(context, attributeSet, theme);
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final ChameleonEditText.Appearance a = (ChameleonEditText.Appearance) appearance;
        ChameleonEditText.Appearance.apply(this, a);
    }

}
