package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by mariotaku on 14/11/5.
 */
public class ActionIconButton extends ImageButton {

    public ActionIconButton(Context context) {
        this(context, null);
    }

    public ActionIconButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public ActionIconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorForeground});
        setColorFilter(a.getColor(0, 0), Mode.SRC_ATOP);
        a.recycle();
    }
}
