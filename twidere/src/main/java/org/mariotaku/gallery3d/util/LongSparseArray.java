/*
 * Copyright (C) 2009 The Android Open Source Project
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

// Copied from android.util.LongSparseArray for unbundling

/**
 * SparseArray mapping longs to Objects. Unlike a normal array of Objects, there
 * can be gaps in the indices. It is intended to be more efficient than using a
 * HashMap to map Longs to Objects.
 */
public class LongSparseArray<E> implements Cloneable {
	private static final Object DELETED = new Object();
	private boolean mGarbage = false;

	private long[] mKeys;
	private Object[] mValues;
	private int mSize;

	/**
	 * Creates a new LongSparseArray containing no mappings.
	 */
	public LongSparseArray() {
		this(10);
	}

	/**
	 * Creates a new LongSparseArray containing no mappings that will not
	 * require any additional memory allocation to store the specified number of
	 * mappings.
	 */
	private LongSparseArray(int initialCapacity) {
		initialCapacity = idealLongArraySize(initialCapacity);

		mKeys = new long[initialCapacity];
		mValues = new Object[initialCapacity];
		mSize = 0;
	}

	/**
	 * Removes all key-value mappings from this LongSparseArray.
	 */
	public void clear() {
		final int n = mSize;
		final Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			values[i] = null;
		}

		mSize = 0;
		mGarbage = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public LongSparseArray<E> clone() {
		LongSparseArray<E> clone = null;
		try {
			clone = (LongSparseArray<E>) super.clone();
			clone.mKeys = mKeys.clone();
			clone.mValues = mValues.clone();
		} catch (final CloneNotSupportedException cnse) {
			/* ignore */
		}
		return clone;
	}

	/**
	 * Gets the Object mapped from the specified key, or <code>null</code> if no
	 * such mapping has been made.
	 */
	public E get(final long key) {
		return get(key, null);
	}

	/**
	 * Adds a mapping from the specified key to the specified value, replacing
	 * the previous mapping from the specified key if there was one.
	 */
	public void put(final long key, final E value) {
		int i = binarySearch(mKeys, 0, mSize, key);

		if (i >= 0) {
			mValues[i] = value;
		} else {
			i = ~i;

			if (i < mSize && mValues[i] == DELETED) {
				mKeys[i] = key;
				mValues[i] = value;
				return;
			}

			if (mGarbage && mSize >= mKeys.length) {
				gc();

				// Search again because indices may have changed.
				i = ~binarySearch(mKeys, 0, mSize, key);
			}

			if (mSize >= mKeys.length) {
				final int n = idealLongArraySize(mSize + 1);

				final long[] nkeys = new long[n];
				final Object[] nvalues = new Object[n];

				// Log.e("SparseArray", "grow " + mKeys.length + " to " + n);
				System.arraycopy(mKeys, 0, nkeys, 0, mKeys.length);
				System.arraycopy(mValues, 0, nvalues, 0, mValues.length);

				mKeys = nkeys;
				mValues = nvalues;
			}

			if (mSize - i != 0) {
				// Log.e("SparseArray", "move " + (mSize - i));
				System.arraycopy(mKeys, i, mKeys, i + 1, mSize - i);
				System.arraycopy(mValues, i, mValues, i + 1, mSize - i);
			}

			mKeys[i] = key;
			mValues[i] = value;
			mSize++;
		}
	}

	/**
	 * Removes the mapping at the specified index.
	 */
	public void removeAt(final int index) {
		if (mValues[index] != DELETED) {
			mValues[index] = DELETED;
			mGarbage = true;
		}
	}

	/**
	 * Returns the number of key-value mappings that this LongSparseArray
	 * currently stores.
	 */
	public int size() {
		if (mGarbage) {
			gc();
		}

		return mSize;
	}

	/**
	 * Given an index in the range <code>0...size()-1</code>, returns the value
	 * from the <code>index</code>th key-value mapping that this LongSparseArray
	 * stores.
	 */
	@SuppressWarnings("unchecked")
	public E valueAt(final int index) {
		if (mGarbage) {
			gc();
		}

		return (E) mValues[index];
	}

	private void gc() {
		// Log.e("SparseArray", "gc start with " + mSize);

		final int n = mSize;
		int o = 0;
		final long[] keys = mKeys;
		final Object[] values = mValues;

		for (int i = 0; i < n; i++) {
			final Object val = values[i];

			if (val != DELETED) {
				if (i != o) {
					keys[o] = keys[i];
					values[o] = val;
					values[i] = null;
				}

				o++;
			}
		}

		mGarbage = false;
		mSize = o;

		// Log.e("SparseArray", "gc end with " + mSize);
	}

	/**
	 * Gets the Object mapped from the specified key, or the specified Object if
	 * no such mapping has been made.
	 */
	@SuppressWarnings("unchecked")
	private E get(final long key, final E valueIfKeyNotFound) {
		final int i = binarySearch(mKeys, 0, mSize, key);

		if (i < 0 || mValues[i] == DELETED)
			return valueIfKeyNotFound;
		else
			return (E) mValues[i];
	}

	private static int binarySearch(final long[] a, final int start, final int len, final long key) {
		int high = start + len, low = start - 1, guess;

		while (high - low > 1) {
			guess = (high + low) / 2;

			if (a[guess] < key) {
				low = guess;
			} else {
				high = guess;
			}
		}

		if (high == start + len)
			return ~(start + len);
		else if (a[high] == key)
			return high;
		else
			return ~high;
	}

	private static int idealByteArraySize(final int need) {
		for (int i = 4; i < 32; i++)
			if (need <= (1 << i) - 12) return (1 << i) - 12;

		return need;
	}

	private static int idealLongArraySize(final int need) {
		return idealByteArraySize(need * 8) / 8;
	}
}
