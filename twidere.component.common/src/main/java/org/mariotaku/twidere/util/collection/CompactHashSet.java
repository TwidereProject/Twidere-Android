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

package org.mariotaku.twidere.util.collection;

// WARNING: If you do any changes to this class, make sure that you
// update CompactIdentityHashSet.java, UniqueSet.java and
// SoftHashMapIndex.java accordingly.

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * INTERNAL: Implements the Set interface more compactly than
 * java.util.HashSet by using a closed hashtable.
 */
public class CompactHashSet<E> extends java.util.AbstractSet<E> {

    protected final static int INITIAL_SIZE = 3;
    protected final static double LOAD_FACTOR = 0.75;

    /**
     * This object is used to represent null, should clients add that to the set.
     */
    protected final static Object nullObject = new Object();
    /**
     * When an object is deleted this object is put into the hashtable
     * in its place, so that other objects with the same key
     * (collisions) further down the hashtable are not lost after we
     * delete an object in the collision chain.
     */
    protected final static Object deletedObject = new Object();
    protected int elements;
    /**
     * This is the number of empty (null) cells. It's not necessarily
     * the same as objects.length - elements, because some cells may
     * contain deletedObject.
     */
    protected int freecells;
    protected E[] objects;
    protected int modCount;

    /**
     * Constructs a new, empty set.
     */
    public CompactHashSet() {
        this(INITIAL_SIZE);
    }

    /**
     * Constructs a new, empty set.
     */
    @SuppressWarnings("unchecked")
    public CompactHashSet(int size) {
        // NOTE: If array size is 0, we get a
        // "java.lang.ArithmeticException: / by zero" in add(Object).
        objects = (E[]) new Object[(size == 0 ? 1 : size)];
        elements = 0;
        freecells = objects.length;
        modCount = 0;
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection.
     *
     * @param c the collection whose elements are to be placed into this set.
     */
    public CompactHashSet(Collection<E> c) {
        this(c.size());
        addAll(c);
    }

    // ===== SET IMPLEMENTATION =============================================

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     * @see ConcurrentModificationException
     */
    @Override
    @NonNull
    public Iterator<E> iterator() {
        return new CompactHashIterator<>();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     */
    @Override
    public int size() {
        return elements;
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     */
    @Override
    public boolean isEmpty() {
        return elements == 0;
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) o = nullObject;

        int hash = o.hashCode();
        int index = (hash & 0x7FFFFFFF) % objects.length;
        int offset = 1;

        // search for the object (continue while !null and !this object)
        while (objects[index] != null &&
                !(objects[index].hashCode() == hash &&
                        objects[index].equals(o))) {
            index = ((index + offset) & 0x7FFFFFFF) % objects.length;
            offset = offset * 2 + 1;

            if (offset == -1)
                offset = 2;
        }

        return objects[index] != null;
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     * element.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean add(Object o) {
        if (o == null) o = nullObject;

        int hash = o.hashCode();
        int index = (hash & 0x7FFFFFFF) % objects.length;
        int offset = 1;
        int deletedix = -1;

        // search for the object (continue while !null and !this object)
        while (objects[index] != null &&
                !(objects[index].hashCode() == hash &&
                        objects[index].equals(o))) {

            // if there's a deleted object here we can put this object here,
            // provided it's not in here somewhere else already
            if (objects[index] == deletedObject)
                deletedix = index;

            index = ((index + offset) & 0x7FFFFFFF) % objects.length;
            offset = offset * 2 + 1;

            if (offset == -1)
                offset = 2;
        }

        if (objects[index] == null) { // wasn't present already
            if (deletedix != -1) // reusing a deleted cell
                index = deletedix;
            else
                freecells--;

            modCount++;
            elements++;

            // here we face a problem regarding generics:
            // add(E o) is not possible because of the null Object. We cant do 'new E()' or '(E) new Object()'
            // so adding an empty object is a problem here
            // If (! o instanceof E) : This will cause a class cast exception
            // If (o instanceof E) : This will work fine

            objects[index] = (E) o;

            // do we need to rehash?
            if (1 - (freecells / (double) objects.length) > LOAD_FACTOR)
                rehash();
            return true;
        } else // was there already
            return false;
    }

    /**
     * Removes the specified element from the set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (o == null) o = nullObject;

        int hash = o.hashCode();
        int index = (hash & 0x7FFFFFFF) % objects.length;
        int offset = 1;

        // search for the object (continue while !null and !this object)
        while (objects[index] != null &&
                !(objects[index].hashCode() == hash &&
                        objects[index].equals(o))) {
            index = ((index + offset) & 0x7FFFFFFF) % objects.length;
            offset = offset * 2 + 1;

            if (offset == -1)
                offset = 2;
        }

        // we found the right position, now do the removal
        if (objects[index] != null) {
            // we found the object

            // same problem here as with add
            objects[index] = (E) deletedObject;
            modCount++;
            elements--;
            return true;
        } else
            // we did not find the object
            return false;
    }

    /**
     * Removes all of the elements from this set.
     */
    @Override
    public void clear() {
        elements = 0;
        for (int ix = 0; ix < objects.length; ix++)
            objects[ix] = null;
        freecells = objects.length;
        modCount++;
    }

    @Override
    @NonNull
    public Object[] toArray() {
        Object[] result = new Object[elements];
        Object[] objects = this.objects;
        int pos = 0;
        for (Object object : objects)
            if (object != null && object != deletedObject) {
                if (object == nullObject)
                    result[pos++] = null;
                else
                    result[pos++] = object;
            }
        // unchecked because it should only contain E
        return result;
    }

    // not sure if this needs to have generics
    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(@NonNull T[] a) {
        int size = elements;
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        E[] objects = this.objects;
        int pos = 0;
        for (E object : objects)
            if (object != null && object != deletedObject) {
                if (object == nullObject)
                    a[pos++] = null;
                else
                    a[pos++] = (T) object;
            }
        return a;
    }

    // ===== INTERNAL METHODS ===============================================

    /**
     * INTERNAL: Used for debugging only.
     */
    public void dump() {
        System.out.println("Size: " + objects.length);
        System.out.println("Elements: " + elements);
        System.out.println("Free cells: " + freecells);
        System.out.println();
        for (int ix = 0; ix < objects.length; ix++)
            System.out.println("[" + ix + "]: " + objects[ix]);
    }

    /**
     * INTERNAL: Figures out correct size for rehashed set, then does
     * the rehash.
     */
    protected void rehash() {
        // do we need to increase capacity, or are there so many
        // deleted objects hanging around that rehashing to the same
        // size is sufficient? if 5% (arbitrarily chosen number) of
        // cells can be freed up by a rehash, we do it.

        int gargagecells = objects.length - (elements + freecells);
        if (gargagecells / (double) objects.length > 0.05)
            // rehash with same size
            rehash(objects.length);
        else
            // rehash with increased capacity
            rehash(objects.length * 2 + 1);
    }

    /**
     * INTERNAL: Rehashes the hashset to a bigger size.
     */
    @SuppressWarnings("unchecked")
    protected void rehash(int newCapacity) {
        @SuppressWarnings("unchecked")
        E[] newObjects = (E[]) new Object[newCapacity];

        for (E o : objects) {
            if (o == null || o == deletedObject)
                continue;

            int hash = o.hashCode();
            int index = (hash & 0x7FFFFFFF) % newCapacity;
            int offset = 1;

            // search for the object
            while (newObjects[index] != null) { // no need to test for duplicates
                index = ((index + offset) & 0x7FFFFFFF) % newCapacity;
                offset = offset * 2 + 1;

                if (offset == -1)
                    offset = 2;
            }

            newObjects[index] = (E) o;
        }

        objects = newObjects;
        freecells = objects.length - elements;
    }

    // ===== ITERATOR IMPLEMENTATON =========================================

    private class CompactHashIterator<T> implements Iterator<T> {
        private int index;
        private int lastReturned = -1;

        /**
         * The modCount value that the iterator believes that the backing
         * CompactHashSet should have.  If this expectation is violated,
         * the iterator has detected concurrent modification.
         */
        private int expectedModCount;

        @SuppressWarnings("empty-statement")
        public CompactHashIterator() {
            for (index = 0; index < objects.length &&
                    (objects[index] == null ||
                            objects[index] == deletedObject); index++)
                ;
            expectedModCount = modCount;
        }

        @Override
        public boolean hasNext() {
            return index < objects.length;
        }

        @Override
        @SuppressWarnings({"empty-statement", "unchecked"})
        public T next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            int length = objects.length;
            if (index >= length) {
                lastReturned = -2;
                throw new NoSuchElementException();
            }

            lastReturned = index;
            for (index += 1; index < length &&
                    (objects[index] == null ||
                            objects[index] == deletedObject); index++)
                ;
            if (objects[lastReturned] == nullObject)
                return null;
            else
                return (T) objects[lastReturned];
        }

        @Override
        @SuppressWarnings("unchecked")
        public void remove() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (lastReturned == -1 || lastReturned == -2)
                throw new IllegalStateException();
            // delete object
            if (objects[lastReturned] != null && objects[lastReturned] != deletedObject) {
                objects[lastReturned] = (E) deletedObject;
                elements--;
                modCount++;
                expectedModCount = modCount; // this is expected; we made the change
            }
        }
    }

}