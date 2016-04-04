package org.mariotaku.twidere.menu;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ActionMenuView;
import android.view.MenuItem;
import android.view.View;

import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable.Style;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 16/2/18.
 */
public class FavoriteItemProvider extends ActionProvider {
    private int mDefaultColor, mActivatedColor;
    private boolean mUseStar;
    private int mIcon;

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public FavoriteItemProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    public void setUseStar(boolean useStar) {
        mUseStar = useStar;
    }

    public void setDefaultColor(int defaultColor) {
        mDefaultColor = defaultColor;
    }

    public void setActivatedColor(int activatedColor) {
        mActivatedColor = activatedColor;
    }

    public void invokeItem(MenuItem item, LikeAnimationDrawable.OnLikedListener listener) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = item.getIcon();
        if (icon instanceof LikeAnimationDrawable) {
            ((LikeAnimationDrawable) icon).setOnLikedListener(listener);
            ((LikeAnimationDrawable) icon).start();
        }
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public void init(final ActionMenuView menuBar, MenuItem item) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = ContextCompat.getDrawable(getContext(), mIcon);
        final LikeAnimationDrawable drawable = new LikeAnimationDrawable(icon, mDefaultColor,
                mActivatedColor, mUseStar ? Style.FAVORITE : Style.LIKE);
        drawable.mutate();
        drawable.setCallback(new ViewCallback(menuBar));
        item.setIcon(drawable);
    }

    public void setIsFavorite(MenuItem item, boolean isFavorite) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = item.getIcon();
        if (icon instanceof LikeAnimationDrawable) {
            icon.mutate();
            icon.setColorFilter(isFavorite ? mActivatedColor : mDefaultColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private static class ViewCallback implements Drawable.Callback {
        private final WeakReference<View> mViewRef;

        public ViewCallback(View view) {
            mViewRef = new WeakReference<>(view);
        }

        @Override
        public void invalidateDrawable(Drawable who) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.invalidate();
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.postDelayed(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.post(what);
        }
    }
}