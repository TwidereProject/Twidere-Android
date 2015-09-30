/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.internal.view.StandaloneActionMode;
import android.support.v7.internal.view.SupportActionModeWrapper;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.view.ActionMode;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.accessibility.AccessibilityEvent;
import android.widget.PopupWindow;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.view.TintedStatusNativeActionModeAwareLayout;

/**
 * Created by mariotaku on 15/4/27.
 */
public class TwidereActionModeForChildListener implements TintedStatusNativeActionModeAwareLayout.OnActionModeForChildListener {
    private final Activity mActivity;
    private final IThemedActivity mThemed;
    private final AppCompatCallback mAppCompatCallback;
    private final Window mWindow;

    private ActionMode mActionMode;
    public ActionBarContextView mActionModeView;
    public PopupWindow mActionModePopup;
    public Runnable mShowActionModePopup;

    public TwidereActionModeForChildListener(IThemedActivity activity, AppCompatCallback callback, boolean usePopup) {
        mActivity = (Activity) activity;
        mThemed = activity;
        mWindow = mActivity.getWindow();
        mAppCompatCallback = callback;
    }

    @Override
    public android.view.ActionMode startActionModeForChild(View originalView, android.view.ActionMode.Callback callback) {
        // Try and start a support action mode, wrapping the callback
        final ActionMode supportActionMode = startSupportActionMode(
                new SupportActionModeWrapper.CallbackWrapper(mActivity, callback));
        if (supportActionMode == null) {
            return mActivity.startActionMode(callback);
        }
        return new SupportActionModeWrapper(mActivity, supportActionMode);
    }

    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("ActionMode callback can not be null.");
        }

        if (mActionMode != null) {
            mActionMode.finish();
        }

        final ActionMode.Callback wrappedCallback = new ActionModeCallbackWrapper(callback);

        if (mActionMode == null) {
            // If the action bar didn't provide an action mode, start the emulated window one
            mActionMode = startSupportActionModeFromWindow(wrappedCallback);
        }

        return mActionMode;
    }

    ActionMode startSupportActionModeFromWindow(ActionMode.Callback callback) {
        if (mActionMode != null) {
            mActionMode.finish();
        }

        final ActionMode.Callback wrappedCallback = new ActionModeCallbackWrapper(callback);

        if (mActionModeView == null) {
            // Use the action bar theme.
            final Context actionBarContext;
            actionBarContext = ThemeUtils.getActionBarThemedContext(mActivity, mThemed.getCurrentThemeResourceId(),
                    mThemed.getCurrentThemeColor());

            mActionModeView = new ActionBarContextView(actionBarContext);
            mActionModePopup = new PopupWindow(actionBarContext, null,
                    android.support.v7.appcompat.R.attr.actionModePopupWindowStyle);
            mActionModePopup.setContentView(mActionModeView);
            mActionModePopup.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);

            final TypedValue outValue = new TypedValue();
            actionBarContext.getTheme().resolveAttribute(
                    android.support.v7.appcompat.R.attr.actionBarSize, outValue, true);
            final int height = TypedValue.complexToDimensionPixelSize(outValue.data,
                    actionBarContext.getResources().getDisplayMetrics());
            mActionModeView.setContentHeight(height);
            ThemeUtils.setActionBarContextViewBackground(mActionModeView,
                    mThemed.getCurrentThemeResourceId(), mThemed.getCurrentThemeColor(),
                    mThemed.getCurrentThemeBackgroundOption(), false);
            mActionModePopup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            final int actionModeOffset = getActionModeOffset();
            mShowActionModePopup = new Runnable() {
                @Override
                public void run() {
                    mActionModePopup.showAtLocation(
                            mWindow.getDecorView(),
                            Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, actionModeOffset);
                }
            };
        }

        if (mActionModeView != null) {
            mActionModeView.killMode();
            ActionMode mode = new StandaloneActionMode(mActionModeView.getContext(),
                    mActionModeView, wrappedCallback, mActionModePopup == null);
            if (callback.onCreateActionMode(mode, mode.getMenu())) {
                mode.invalidate();
                mActionModeView.initForMode(mode);
                mActionModeView.setVisibility(View.VISIBLE);
                mActionMode = mode;
                if (mActionModePopup != null) {
                    mWindow.getDecorView().post(mShowActionModePopup);
                }
                mActionModeView.sendAccessibilityEvent(
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

                if (mActionModeView.getParent() != null) {
                    ViewCompat.requestApplyInsets((View) mActionModeView.getParent());
                }
            } else {
                mActionMode = null;
            }

        }
        if (mActionMode != null && mAppCompatCallback != null) {
            mAppCompatCallback.onSupportActionModeStarted(mActionMode);
        }
        return mActionMode;
    }

    private int getActionModeOffset() {
        if (mActivity instanceof IBaseFragment.SystemWindowsInsetsCallback) {
            final Rect insets = new Rect();
            if (((IBaseFragment.SystemWindowsInsetsCallback) mActivity).getSystemWindowsInsets(insets)) {
                return Utils.getInsetsTopWithoutActionBarHeight(mActivity, insets.top);
            }
        }
        return 0;
    }

    public boolean finishExisting() {
        if (mActionMode != null) {
            mActionMode.finish();
            return true;
        }
        return false;
    }


    /**
     * Clears out internal reference when the action mode is destroyed.
     */
    class ActionModeCallbackWrapper implements ActionMode.Callback {
        private ActionMode.Callback mWrapped;

        public ActionModeCallbackWrapper(ActionMode.Callback wrapped) {
            mWrapped = wrapped;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onCreateActionMode(mode, menu);
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return mWrapped.onActionItemClicked(mode, item);
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mWrapped.onDestroyActionMode(mode);
            if (mActionModePopup != null) {
                mWindow.getDecorView().removeCallbacks(mShowActionModePopup);
                mActionModePopup.dismiss();
            } else if (mActionModeView != null) {
                mActionModeView.setVisibility(View.GONE);
                if (mActionModeView.getParent() != null) {
                    ViewCompat.requestApplyInsets((View) mActionModeView.getParent());
                }
            }
            if (mActionModeView != null) {
                mActionModeView.removeAllViews();
            }
            if (mAppCompatCallback != null) {
                mAppCompatCallback.onSupportActionModeFinished(mActionMode);
            }
            mActionMode = null;
        }
    }

}
