package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by mariotaku on 14/11/5.
 */
public class MediaSizeImageView extends ImageView {

    private int mMediaWidth, mMediaHeight;

    public MediaSizeImageView(final Context context) {
        this(context, null);
    }

    public MediaSizeImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaSizeImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMediaSize(int width, int height) {
        mMediaWidth = width;
        mMediaHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        if (mMediaWidth == 0 || mMediaHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        final float whRatio = (float) mMediaWidth / mMediaHeight;
        final int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
        final ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            final int calcWidth = Math.round(height * whRatio);
            super.onMeasure(MeasureSpec.makeMeasureSpec(calcWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
            setMeasuredDimension(calcWidth, height);
        } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            final int calcHeight = Math.round(width / whRatio);
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(calcHeight, MeasureSpec.EXACTLY));
            setMeasuredDimension(width, calcHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
