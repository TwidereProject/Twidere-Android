/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.util;

import android.graphics.Matrix;
import android.view.MotionEvent;

public final class MotionEventHelper {

	private MotionEventHelper() {
		throw new AssertionError();
	}

	public static MotionEvent transformEvent(final MotionEvent e, final Matrix m) {
		final MotionEvent newEvent = MotionEvent.obtain(e);
		newEvent.transform(m);
		return newEvent;
	}
}
