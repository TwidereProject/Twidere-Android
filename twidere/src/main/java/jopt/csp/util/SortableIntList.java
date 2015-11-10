/*
 *                 Twidere - Twitter client for Android
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

/*
 * Created on Nov 21, 2005
 */
package jopt.csp.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntCollection;
import org.apache.commons.collections.primitives.IntList;
import org.apache.commons.collections.primitives.RandomAccessIntList;


/**
 * A flexible, sortable list of int primitives.  Borrows much
 * of its functionality from the ArrayIntList implementation
 * given in the Commons Primitives project
 * (http://jakarta.apache.org/commons/primitives/index.html)
 *
 * @author Chris Johnson
 */
public class SortableIntList extends RandomAccessIntList implements IntList, Serializable {

    private transient int[] data = null;
    private int size = 0;

    /**
     * Construct an empty list with the default
     * initial capacity.
     */
    public SortableIntList() {
        this(8);
    }

    /**
     * Construct an empty list with the given
     * initial capacity.
     *
     * @throws IllegalArgumentException when <i>initialCapacity</i> is negative
     */
    public SortableIntList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("capacity " + initialCapacity + " cannot be negative");
        }
        data = new int[initialCapacity];
        size = 0;
    }

    /**
     * Constructs a list containing the elements of the given collection,
     * in the order they are returned by that collection's iterator.
     *
     * @param that the non-<code>null</code> collection of <code>int</code>s
     *             to add
     * @throws NullPointerException if <i>that</i> is <code>null</code>
     * @see ArrayIntList#addAll(org.apache.commons.collections.primitives.IntCollection)
     */
    public SortableIntList(IntCollection that) {
        this(that.size());
        addAll(that);
    }

    @Override
    public int get(int index) {
        checkRange(index);
        return data[index];
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Removes the element at the specified position in
     * (optional operation).  Any subsequent elements
     * are shifted to the left, subtracting one from their
     * indices.  Returns the element that was removed.
     *
     * @param index the index of the element to remove
     * @return the value of the element that was removed
     * @throws UnsupportedOperationException when this operation is not
     *                                       supported
     * @throws IndexOutOfBoundsException     if the specified index is out of range
     */
    @Override
    public int removeElementAt(int index) {
        checkRange(index);
        incrModCount();
        int oldval = data[index];
        int numtomove = size - index - 1;
        if (numtomove > 0) {
            System.arraycopy(data, index + 1, data, index, numtomove);
        }
        size--;
        return oldval;
    }

    /**
     * Replaces the element at the specified
     * position in me with the specified element
     * (optional operation). If specified index is
     * beyond the current size, the list grows
     * to accommodate it.  No IndexOutOfBoundsException
     * will occur during the set operation.
     *
     * @param index   the index of the element to change
     * @param element the value to be stored at the specified position
     * @return the value previously stored at the specified position
     * @throws UnsupportedOperationException when this operation is not
     *                                       supported
     */
    @Override
    public int set(int index, int element) {
        ensureCapacity(index + 1);
        ensureSize(index + 1);
        incrModCount();
        int oldval = data[index];
        data[index] = element;
        return oldval;
    }

    /**
     * Inserts the specified element at the specified position
     * (optional operation). Shifts the element currently
     * at that position (if any) and any subsequent elements to the
     * right, increasing their indices.  If the specified index is
     * beyond the current size, this method behaves like a call
     * to {@link #set(int, int)}.
     *
     * @param index   the index at which to insert the element
     * @param element the value to insert
     * @throws UnsupportedOperationException when this operation is not
     *                                       supported
     * @throws IllegalArgumentException      if some aspect of the specified element
     *                                       prevents it from being added to me
     */
    @Override
    public void add(int index, int element) {
        if (index >= size) {
            set(index, element);
        } else {
            incrModCount();
            ensureCapacity(size + 1);
            int numtomove = size - index;
            System.arraycopy(data, index, data, index + 1, numtomove);
            data[index] = element;
            size++;
        }
    }

    /**
     * Increases my capacity, if necessary, to ensure that I can hold at
     * least the number of elements specified by the minimum capacity
     * argument without growing.
     */
    public void ensureCapacity(int mincap) {
        incrModCount();
        if (mincap > data.length) {
            int newcap = (data.length * 3) / 2 + 1;
            int[] olddata = data;
            data = new int[newcap < mincap ? mincap : newcap];
            System.arraycopy(olddata, 0, data, 0, size);
        }
    }

    /**
     * Reduce my capacity, if necessary, to match my
     * current {@link #size size}.
     */
    public void trimToSize() {
        incrModCount();
        if (size < data.length) {
            int[] olddata = data;
            data = new int[size];
            System.arraycopy(olddata, 0, data, 0, size);
        }
    }

    /**
     * Sorts the list into ascending numerical order via {@link java.util.Arrays#sort(int[])}
     * <p/>
     * Sorts the list of ints into ascending numerical order. The sorting algorithm
     * is a tuned quicksort, adapted from Jon L. Bentley and M. Douglas McIlroy's "Engineering
     * a Sort Function", Software-Practice and Experience, Vol. 23(11) P. 1249-1265 (November
     * 1993). This algorithm offers n*log(n) performance on many data sets that cause other
     * quicksorts to degrade to quadratic performance.
     */
    public void sort() {
        trimToSize();
        Arrays.sort(data);
    }

    /**
     * Reverses the order of the elements
     */
    public void reverse() {
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--)
            swap(i, j);
    }

    /**
     * Swaps the two specified elements.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     */
    public void swap(int i, int j) {
        int tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }

    /**
     * Searches the list for the specified key via {@link java.util.Arrays#binarySearch(int[], int)}
     * <p/>
     * The array must be sorted (as by the sort method, above) prior to making this call.
     * If it is not sorted, the results are undefined. If the list contains multiple elements
     * with the specified value, there is no guarantee which one will be found.
     *
     * @param key the value to be searched for
     * @return index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1)
     */
    public int binarySearch(int key) {
        trimToSize();
        return Arrays.binarySearch(data, key);
    }

    /**
     * Sorts the specified range of the list into ascending numerical order
     * via {@link java.util.Arrays#sort(int[], int, int)}
     *
     * @param fromIndex the index of the first element (inclusive) to be sorted
     * @param toIndex   the index of the last element (exclusive) to be sorted
     */
    public void sort(int fromIndex, int toIndex) {
        trimToSize();
        Arrays.sort(data, fromIndex, toIndex);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(data.length);
        for (int i = 0; i < size; i++) {
            out.writeInt(data[i]);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        data = new int[in.readInt()];
        for (int i = 0; i < size; i++) {
            data[i] = in.readInt();
        }
    }

    private void ensureSize(int potentialSize) {
        if (potentialSize > size) {
            size = potentialSize;
        }
    }

    private void checkRange(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Should be at least 0 and less than " + size + ", found " + index);
        }
    }

}
