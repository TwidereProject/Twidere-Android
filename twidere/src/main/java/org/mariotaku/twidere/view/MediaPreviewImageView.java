package org.mariotaku.twidere.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.IForegroundView;

/**
 * Created by mariotaku on 16/1/30.
 */
public class MediaPreviewImageView extends AppCompatImageView {

    private final IForegroundView.ForegroundViewHelper mForegroundViewHelper;

    private boolean mHasPlayIcon;

    public MediaPreviewImageView(Context context) {
        this(context, null);
    }

    public MediaPreviewImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaPreviewImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mForegroundViewHelper = new IForegroundView.ForegroundViewHelper(this, context, attrs, defStyle);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updatePlayIcon();
    }

    protected void updatePlayIcon() {
        if (mForegroundViewHelper == null) return;
        if (mHasPlayIcon && getDrawable() != null) {
            mForegroundViewHelper.setForeground(ContextCompat.getDrawable(getContext(), R.drawable.ic_card_media_play));
        } else {
            mForegroundViewHelper.setForeground(null);
        }
    }

    public void setHasPlayIcon(boolean hasPlayIcon) {
        mHasPlayIcon = hasPlayIcon;
        updatePlayIcon();
    }


    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnLayout(changed, left, top, right, bottom);
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(@NonNull final Drawable who) {
        return super.verifyDrawable(who) || (mForegroundViewHelper != null && mForegroundViewHelper.verifyDrawable(who));
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.jumpDrawablesToCurrentState();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.drawableStateChanged();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y) {
        super.drawableHotspotChanged(x, y);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchDrawableHotspotChanged(x, y);
        }
    }

    @Override
    protected void onDraw(@NonNull final Canvas canvas) {
        super.onDraw(canvas);
        if (mForegroundViewHelper != null) {
            mForegroundViewHelper.dispatchOnDraw(canvas);
        }
    }
}
