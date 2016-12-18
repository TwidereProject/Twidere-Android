package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import org.mariotaku.multivalueswitch.library.MultiValueSwitch;

public class ThemedMultiValueSwitch extends MultiValueSwitch {

    public ThemedMultiValueSwitch(Context context) {
        super(context);
    }

    public ThemedMultiValueSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isShown() {
        return getParent() != null && getVisibility() == View.VISIBLE;
    }


}
