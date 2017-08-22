/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by mariotaku on 2017/8/20.
 */

public abstract class ChoreographerCompat {

    private ChoreographerCompat() {

    }

    public abstract void postFrameCallback(ChoreographerCompat.FrameCallback callback);

    public abstract void postFrameCallbackDelayed(ChoreographerCompat.FrameCallback callback, long delayMillis);

    public abstract void removeFrameCallback(ChoreographerCompat.FrameCallback callback);

    public static ChoreographerCompat getInstance() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return ChoreographerCompatImpl.getCompatInstance();
        }
        return ChoreographerNativeDelegate.getNativeInstance();
    }

    public interface FrameCallback {
        void doFrame(long frameTimeNanos);
    }

    private static class ChoreographerCompatImpl extends ChoreographerCompat {

        private static final Map<Looper, ChoreographerCompat> sInstances = new WeakHashMap<>();

        private final Handler handler;

        ChoreographerCompatImpl(Looper looper) {
            handler = new Handler(looper);
        }

        @Override
        public void postFrameCallback(FrameCallback callback) {
            handler.postDelayed(CompatFrameCallbackWrapper.wrap(callback), 0);
        }

        @Override
        public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
            handler.postDelayed(CompatFrameCallbackWrapper.wrap(callback), delayMillis + 16);
        }

        @Override
        public void removeFrameCallback(FrameCallback callback) {
            handler.removeCallbacks(CompatFrameCallbackWrapper.wrap(callback));
        }

        static ChoreographerCompat getCompatInstance() {
            final Looper looper = Looper.myLooper();
            ChoreographerCompat instance = sInstances.get(looper);
            if (instance != null) return instance;
            instance = new ChoreographerCompatImpl(looper);
            sInstances.put(looper, instance);
            return instance;
        }


        private static class CompatFrameCallbackWrapper implements Runnable {
            private static final Map<FrameCallback, Runnable> sInstances = new WeakHashMap<>();
            private final FrameCallback callback;

            private CompatFrameCallbackWrapper(ChoreographerCompat.FrameCallback callback) {
                this.callback = callback;
            }

            @Override
            public void run() {
                callback.doFrame(System.nanoTime());
            }

            static Runnable wrap(FrameCallback callback) {
                Runnable wrapper = sInstances.get(callback);
                if (wrapper != null) return wrapper;
                wrapper = new CompatFrameCallbackWrapper(callback);
                sInstances.put(callback, wrapper);
                return wrapper;
            }

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static class ChoreographerNativeDelegate extends ChoreographerCompat {

        private static final Map<Choreographer, ChoreographerCompat> sInstances = new WeakHashMap<>();

        private final Choreographer implementation;

        ChoreographerNativeDelegate(Choreographer implementation) {
            this.implementation = implementation;
        }

        public void postFrameCallback(ChoreographerCompat.FrameCallback callback) {
            implementation.postFrameCallback(NativeFrameCallbackWrapper.wrap(callback));
        }

        public void postFrameCallbackDelayed(ChoreographerCompat.FrameCallback callback, long delayMillis) {
            implementation.postFrameCallbackDelayed(NativeFrameCallbackWrapper.wrap(callback), delayMillis);
        }

        public void removeFrameCallback(ChoreographerCompat.FrameCallback callback) {
            implementation.removeFrameCallback(NativeFrameCallbackWrapper.wrap(callback));
        }

        static ChoreographerCompat getNativeInstance() {
            final Choreographer implementation = Choreographer.getInstance();
            ChoreographerCompat instance = sInstances.get(implementation);
            if (instance != null) return instance;
            instance = new ChoreographerNativeDelegate(implementation);
            sInstances.put(implementation, instance);
            return instance;
        }

        private static class NativeFrameCallbackWrapper implements Choreographer.FrameCallback {
            private static final Map<FrameCallback, Choreographer.FrameCallback> sInstances = new WeakHashMap<>();
            private final FrameCallback callback;

            private NativeFrameCallbackWrapper(ChoreographerCompat.FrameCallback callback) {
                this.callback = callback;
            }

            @Override
            public void doFrame(long frameTimeNanos) {
                callback.doFrame(frameTimeNanos);
            }

            static Choreographer.FrameCallback wrap(ChoreographerCompat.FrameCallback callback) {
                Choreographer.FrameCallback wrapper = sInstances.get(callback);
                if (wrapper != null) return wrapper;
                wrapper = new NativeFrameCallbackWrapper(callback);
                sInstances.put(callback, wrapper);
                return wrapper;
            }

        }
    }
}
