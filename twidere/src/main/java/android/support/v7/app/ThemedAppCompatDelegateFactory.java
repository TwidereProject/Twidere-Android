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
import android.support.annotation.NonNull;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Window;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemedLayoutInflaterFactory;

/**
 * Created by mariotaku on 15/4/22.
 */
public class ThemedAppCompatDelegateFactory implements Constants {


    /**
     * Create a {@link android.support.v7.app.AppCompatDelegate} to use with {@code activity}.
     *
     * @param callback An optional callback for AppCompat specific events
     */
    public static ThemedAppCompatDelegate create(@NonNull final IThemedActivity themed,
                                                 @NonNull final AppCompatCallback callback) {
        final Activity activity = (Activity) themed;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new ThemedAppCompatDelegate(themed, activity, activity.getWindow(), callback);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ThemedAppCompatDelegate extends AppCompatDelegateImplV11 {

        private final IThemedActivity themed;
        private KeyListener keyListener;

        private ThemedAppCompatDelegate(@NonNull final IThemedActivity themed, @NonNull final Context context,
                                        @NonNull final Window window, @NonNull final AppCompatCallback callback) {
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

        @Override
        boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyListener != null && keyListener.onKeyDown(keyCode, event)) return true;
            return super.onKeyDown(keyCode, event);
        }

        @Override
        boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyListener != null && keyListener.onKeyUp(keyCode, event)) return true;
            return super.onKeyUp(keyCode, event);
        }

        public void setKeyListener(KeyListener listener) {
            keyListener = listener;
        }
    }

    public interface KeyListener {

        boolean onKeyDown(int keyCode, KeyEvent event);

        boolean onKeyUp(int keyCode, KeyEvent event);
    }
}
