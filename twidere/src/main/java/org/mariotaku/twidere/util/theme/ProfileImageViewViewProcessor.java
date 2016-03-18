package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import org.mariotaku.twidere.view.ProfileImageView;
import org.mariotaku.twidere.view.ShapedImageView;

/**
 * Created by mariotaku on 16/3/18.
 */
public class ProfileImageViewViewProcessor implements ViewProcessor<ProfileImageView, Object> {
    @ShapedImageView.ShapeStyle
    private int mStyle = ShapedImageView.SHAPE_CIRCLE;

    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable ProfileImageView target, @Nullable Object extra) {
        if (target == null) return;
        target.setStyle(mStyle);
    }

    public void setStyle(@ShapedImageView.ShapeStyle int style) {
        mStyle = style;
    }
}
