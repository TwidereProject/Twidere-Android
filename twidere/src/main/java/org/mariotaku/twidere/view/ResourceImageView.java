package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14-7-27.
 */
public class ResourceImageView extends ImageButton {

    public ResourceImageView(Context context) {
        this(context, null);
    }

    public ResourceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ResourceImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            final TypedArray array = context.obtainStyledAttributes(attrs,
                    R.styleable.ResourceImageView, defStyle, 0);
            final int resId = array.getResourceId(R.styleable.ResourceImageView_image, -1);
            if (resId > 0) {
                setImageResource(resId);
            }
            array.recycle();
        }
    }
}
