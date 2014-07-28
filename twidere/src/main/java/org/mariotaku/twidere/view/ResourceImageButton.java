package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by mariotaku on 14-7-27.
 */
public class ResourceImageButton extends ResourceImageView {

    public ResourceImageButton(Context context) {
        this(context, null);
    }

    public ResourceImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.imageButtonStyle);
    }

    public ResourceImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFocusable(true);
    }
}
