/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.support.annotation.NonNull;

public class TwidereMathUtils {
    public static final int RANGE_EXCLUSIVE_EXCLUSIVE = 0b00;
    public static final int RANGE_EXCLUSIVE_INCLUSIVE = 0b01;
    public static final int RANGE_INCLUSIVE_EXCLUSIVE = 0b10;
    public static final int RANGE_INCLUSIVE_INCLUSIVE = 0b11;
    static final int MASK_LEFT_BOUND = 0b10;
    static final int MASK_RIGHT_BOUND = 0b01;

    private TwidereMathUtils() {
    }

    public static float clamp(final float num, final float bound1, final float bound2) {
        final float max = Math.max(bound1, bound2), min = Math.min(bound1, bound2);
        return Math.max(Math.min(num, max), min);
    }

    public static int clamp(final int num, final int bound1, final int bound2) {
        final int max = Math.max(bound1, bound2), min = Math.min(bound1, bound2);
        return Math.max(Math.min(num, max), min);
    }

    // Returns the next power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0 or
    // the answer overflows.
    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > 1 << 30) throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    public static int prevPowerOf2(final int n) {
        if (n <= 0) throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    public static double sum(double... doubles) {
        double sum = 0;
        for (double d : doubles) {
            sum += d;
        }
        return sum;
    }

    public static int sum(@NonNull int[] array) {
        return sum(array, 0, array.length - 1);
    }

    public static int sum(@NonNull int[] array, int start, int end) {
        int sum = 0;
        for (int i = start; i <= end; i++) {
            int num = array[i];
            sum += num;
        }
        return sum;
    }

    public static boolean inRange(int num, int from, int to, int flag) {
        return ((flag & MASK_LEFT_BOUND) == 0 ? num > from : num >= from)
                && ((flag & MASK_RIGHT_BOUND) == 0 ? num < to : num <= to);
    }

    public static boolean inRange(float num, float from, float to, int flag) {
        return ((flag & MASK_LEFT_BOUND) == 0 ? num > from : num >= from)
                && ((flag & MASK_RIGHT_BOUND) == 0 ? num < to : num <= to);
    }

}
