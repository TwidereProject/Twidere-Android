/*
 * Copyright (C) 2013 Artur Termenji
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
package com.atermenji.android.iconicdroid.util;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Helper class that wraps icon fonts and manages {@link Typeface} loading.
 */
public class TypefaceManager {

	private TypefaceManager() {
	}

	public static interface IconicTypeface {

		/**
		 * Loads a {@link Typeface} for the given icon font. {@link Typeface} is
		 * loaded only once to avoid memory consumption.
		 * 
		 * @param context
		 * @return {@link Typeface}
		 */
		public Typeface getTypeface(final Context context);
	}
}
