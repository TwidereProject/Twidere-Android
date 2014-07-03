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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectManager implements Constants {

	private final NoDuplicatesArrayList<Long> mSelectedStatusIds = new NoDuplicatesArrayList<Long>();
	private final NoDuplicatesArrayList<Long> mSelectedUserIds = new NoDuplicatesArrayList<Long>();
	private final NoDuplicatesArrayList<Callback> mCallbacks = new NoDuplicatesArrayList<Callback>();
	private final ItemsList mSelectedItems = new ItemsList(this);
	private long mAccountId;

	public void clearSelectedItems() {
		mSelectedItems.clear();
	}

	public long getAccountId() {
		if (mAccountId <= 0) return getFirstSelectAccountId(mSelectedItems);
		return mAccountId;
	}

	public int getCount() {
		return mSelectedItems.size();
	}

	public long getFirstSelectAccountId() {
		return getFirstSelectAccountId(mSelectedItems);
	}

	public List<Object> getSelectedItems() {
		return mSelectedItems;
	}

	public boolean isActive() {
		return !mSelectedItems.isEmpty();
	}

	public boolean isSelected(final Object object) {
		return mSelectedItems.contains(object);
	}

	public boolean isStatusSelected(final long status_id) {
		return mSelectedStatusIds.contains(status_id);
	}

	public boolean isUserSelected(final long user_id) {
		return mSelectedUserIds.contains(user_id);
	}

	public void registerCallback(final Callback callback) {
		if (callback == null) return;
		mCallbacks.add(callback);
	}

	public boolean selectItem(final Object item) {
		return mSelectedItems.add(item);
	}

	public void setAccountId(final long accountId) {
		mAccountId = accountId;
	}

	public void unregisterCallback(final Callback callback) {
		mCallbacks.remove(callback);
	}

	public boolean unselectItem(final Object item) {
		return mSelectedItems.remove(item);
	}

	private void onItemsCleared() {
		for (final Callback callback : mCallbacks) {
			callback.onItemsCleared();
		}
		mAccountId = -1;
	}

	private void onItemSelected(final Object object) {
		for (final Callback callback : mCallbacks) {
			callback.onItemSelected(object);
		}
	}

	private void onItemUnselected(final Object object) {
		for (final Callback callback : mCallbacks) {
			callback.onItemUnselected(object);
		}
	}

	public static long getFirstSelectAccountId(final List<Object> selected_items) {
		final Object obj = selected_items.get(0);
		if (obj instanceof ParcelableUser)
			return ((ParcelableUser) obj).account_id;
		else if (obj instanceof ParcelableStatus) return ((ParcelableStatus) obj).account_id;
		return -1;
	}

	public static long[] getSelectedUserIds(final List<Object> selected_items) {
		final ArrayList<Long> ids_list = new ArrayList<Long>();
		for (final Object item : selected_items) {
			if (item instanceof ParcelableUser) {
				ids_list.add(((ParcelableUser) item).id);
			} else if (item instanceof ParcelableStatus) {
				ids_list.add(((ParcelableStatus) item).user_id);
			}
		}
		return ArrayUtils.fromList(ids_list);
	}

	public static interface Callback {

		public void onItemsCleared();

		public void onItemSelected(Object item);

		public void onItemUnselected(Object item);

	}

	@SuppressWarnings("serial")
	static class ItemsList extends NoDuplicatesArrayList<Object> {

		private final MultiSelectManager manager;

		ItemsList(final MultiSelectManager manager) {
			this.manager = manager;
		}

		@Override
		public boolean add(final Object object) {
			if (object instanceof ParcelableStatus) {
				manager.mSelectedStatusIds.add(((ParcelableStatus) object).id);
			} else if (object instanceof ParcelableUser) {
				manager.mSelectedUserIds.add(((ParcelableUser) object).id);
			} else
				return false;
			final boolean ret = super.add(object);
			manager.onItemSelected(object);
			return ret;
		}

		@Override
		public void clear() {
			super.clear();
			manager.mSelectedStatusIds.clear();
			manager.mSelectedUserIds.clear();
			manager.onItemsCleared();
		}

		@Override
		public boolean remove(final Object object) {
			final boolean ret = super.remove(object);
			if (object instanceof ParcelableStatus) {
				manager.mSelectedStatusIds.remove(((ParcelableStatus) object).id);
			} else if (object instanceof ParcelableUser) {
				manager.mSelectedUserIds.remove(((ParcelableUser) object).id);
			}
			if (ret) {
				if (isEmpty()) {
					manager.onItemsCleared();
				} else {
					manager.onItemUnselected(object);
				}
			}
			return ret;
		}

	}
}
