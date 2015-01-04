package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by mariotaku on 14/11/5.
 */
public class ActionIconView extends ImageView {

    private final int mDefaultColor;

    public ActionIconView(Context context) {
        this(context, null);
    }

    public int getDefaultColor() {
        return mDefaultColor;
    }

    public ActionIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorForeground});
        mDefaultColor = a.getColor(0, 0);
        setColorFilter(mDefaultColor, Mode.SRC_ATOP);
        a.recycle();
    }
}
