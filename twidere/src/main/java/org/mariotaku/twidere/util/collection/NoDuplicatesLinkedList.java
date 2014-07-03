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

package org.mariotaku.twidere.util.collection;

import java.util.Collection;
import java.util.LinkedList;

public class NoDuplicatesLinkedList<E> extends LinkedList<E> {
	private static final long serialVersionUID = 4295241536597752001L;

	public NoDuplicatesLinkedList() {
		super();
	}

	public NoDuplicatesLinkedList(final Collection<? extends E> collection) {
		super(collection);
	}

	@Override
	public boolean add(final E e) {
		if (contains(e))
			return false;
		else
			return super.add(e);
	}

	@Override
	public void add(final int index, final E element) {
		if (contains(element))
			return;
		else {
			super.add(index, element);
		}
	}

	@Override
	public boolean addAll(final Collection<? extends E> collection) {
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(copy);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> collection) {
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(index, copy);
	}
}
