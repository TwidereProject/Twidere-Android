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

package org.mariotaku.twidere.util.support.graphics;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;

/**
 * Created by mariotaku on 15/4/27.
 */
@SuppressWarnings("unused")
public abstract class OutlineCompat {

    public abstract float getAlpha();

    public abstract boolean isEmpty();

    public abstract void set(Outline src);

    public abstract void setAlpha(float alpha);

    public abstract void setConvexPath(Path convexPath);

    public abstract void setOval(int left, int top, int right, int bottom);

    public abstract void setOval(Rect rect);

    public abstract void setEmpty();

    public abstract boolean canClip();

    public abstract void setRect(int left, int top, int right, int bottom);

    public abstract void setRect(Rect rect);

    public abstract void setRoundRect(int left, int top, int right, int bottom, float radius);

    public abstract void setRoundRect(Rect rect, float radius);

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class OutlineL extends OutlineCompat {

        public static Outline getWrapped(OutlineCompat outlineL) {
            return outlineL instanceof OutlineL ? ((OutlineL) outlineL).outline : null;
        }

        private final Outline outline;

        @Override
        public void setEmpty() {
            outline.setEmpty();
        }

        @Override
        public boolean canClip() {
            return outline.canClip();
        }

        @Override
        public void setRect(int left, int top, int right, int bottom) {
            outline.setRect(left, top, right, bottom);
        }

        @Override
        public void setOval(int left, int top, int right, int bottom) {
            outline.setOval(left, top, right, bottom);
        }

        @Override
        public void setConvexPath(Path convexPath) {
            outline.setConvexPath(convexPath);
        }

        @Override
        public void setRect(Rect rect) {
            outline.setRect(rect);
        }

        @Override
        public void setOval(Rect rect) {
            outline.setOval(rect);
        }

        @Override
        public float getAlpha() {
            return outline.getAlpha();
        }

        @Override
        public void set(Outline src) {
            outline.set(src);
        }

        @Override
        public void setRoundRect(int left, int top, int right, int bottom, float radius) {
            outline.setRoundRect(left, top, right, bottom, radius);
        }

        @Override
        public void setRoundRect(Rect rect, float radius) {
            outline.setRoundRect(rect, radius);
        }

        @Override
        public boolean isEmpty() {
            return outline.isEmpty();
        }

        @Override
        public void setAlpha(float alpha) {
            outline.setAlpha(alpha);
        }

        public OutlineL(Outline outline) {
            this.outline = outline;
        }
    }
}
