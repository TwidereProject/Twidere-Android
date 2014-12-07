package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14/11/14.
 */
public class AssetFontTextView extends TextView {
    public AssetFontTextView(Context context) {
        this(context, null);
    }

    public AssetFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AssetFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AssetFontTextView, defStyleAttr, 0);
        final String path = a.getString(R.styleable.AssetFontTextView_fontPath);
        if (path != null && !isInEditMode()) {
            setTypeface(Typeface.createFromAsset(context.getAssets(), path));
        }
        a.recycle();
    }
}
