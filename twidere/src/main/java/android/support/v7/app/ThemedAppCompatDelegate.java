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

package android.support.v7.app;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 15/4/22.
 */
public class ThemedAppCompatDelegate implements Constants {


    /**
     * Create a {@link android.support.v7.app.AppCompatDelegate} to use with {@code activity}.
     *
     * @param callback An optional callback for AppCompat specific events
     */
    public static AppCompatDelegate create(IThemedActivity themed, AppCompatCallback callback) {
        final Activity activity = (Activity) themed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new ThemedAppCompatDelegateImplV11(themed, activity, activity.getWindow(), callback);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static class ThemedAppCompatDelegateImplV11 extends AppCompatDelegateImplV11 {

        private final IThemedActivity themed;

        private ThemedAppCompatDelegateImplV11(IThemedActivity themed, Context context, Window window, AppCompatCallback callback) {
            super(context, window, callback);
            this.themed = themed;
        }

        @Override
        public void installViewFactory() {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            if (inflater.getFactory() == null) {
                LayoutInflaterCompat.setFactory(inflater, new ThemedLayoutInflaterFactory(themed, this));
            }
        }
    }

    /**
     * Created by mariotaku on 15/4/22.
     */
    private static class ThemedLayoutInflaterFactory implements LayoutInflaterFactory {

        private final IThemedActivity activity;
        private final ThemedAppCompatDelegateImplV11 delegate;

        public ThemedLayoutInflaterFactory(IThemedActivity activity, ThemedAppCompatDelegateImplV11 delegate) {
            this.activity = activity;
            this.delegate = delegate;
        }

        @Override
        public View onCreateView(View view, String s, Context context, AttributeSet attributeSet) {
            final View createdView = delegate.onCreateView(view, s, context, attributeSet);
            ThemeUtils.initView(createdView, activity.getCurrentThemeColor(), activity.getCurrentProfileImageStyle());
            return createdView;
        }
    }
}
