/*
 * Copyright (C) 2010 The Android Open Source Project
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

public class IntArray {
	private static final int INIT_CAPACITY = 8;

	private int mData[] = new int[INIT_CAPACITY];
	private int mSize = 0;

	public void add(final int value) {
		if (mData.length == mSize) {
			final int temp[] = new int[mSize + mSize];
			System.arraycopy(mData, 0, temp, 0, mSize);
			mData = temp;
		}
		mData[mSize++] = value;
	}

	public void clear() {
		mSize = 0;
		if (mData.length != INIT_CAPACITY) {
			mData = new int[INIT_CAPACITY];
		}
	}

	public int[] getInternalArray() {
		return mData;
	}

	public int size() {
		return mSize;
	}

}
