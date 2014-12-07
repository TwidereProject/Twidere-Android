package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14/11/5.
 */
public class IconActionButton extends ImageButton {

    private final int mColor, mActivatedColor;

    public IconActionButton(Context context) {
        this(context, null);
    }

    public IconActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray defaultValues = context.obtainStyledAttributes(
                new int[]{android.R.attr.colorForeground, android.R.attr.colorActivatedHighlight});
        final int defaultColor = defaultValues.getColor(0, 0);
        final int defaultActivatedColor = defaultValues.getColor(1, 0);
        defaultValues.recycle();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton);
        mColor = a.getColor(R.styleable.IconActionButton_iabColor, defaultColor);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, defaultActivatedColor);
        a.recycle();
        updateColorFilter();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        updateColorFilter();
    }

    private void updateColorFilter() {
        setColorFilter(isActivated() ? mActivatedColor : mColor, Mode.SRC_ATOP);
    }
}
