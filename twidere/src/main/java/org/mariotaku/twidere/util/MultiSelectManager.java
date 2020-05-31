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

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.ArrayList;
import java.util.List;

public class MultiSelectManager {

    private final NoDuplicatesArrayList<String> mSelectedStatusIds = new NoDuplicatesArrayList<>();
    private final NoDuplicatesArrayList<UserKey> mSelectedUserKeys = new NoDuplicatesArrayList<>();
    private final NoDuplicatesArrayList<Callback> mCallbacks = new NoDuplicatesArrayList<>();
    private final ItemsList mSelectedItems = new ItemsList(this);
    private UserKey mAccountKey;

    public void clearSelectedItems() {
        mSelectedItems.clear();
    }

    public UserKey getAccountKey() {
        if (mAccountKey == null) return getFirstSelectAccountKey(mSelectedItems);
        return mAccountKey;
    }

    public int getCount() {
        return mSelectedItems.size();
    }

    public UserKey getFirstSelectAccountKey() {
        return getFirstSelectAccountKey(mSelectedItems);
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

    public boolean isStatusSelected(final String statusId) {
        return mSelectedStatusIds.contains(statusId);
    }

    public boolean isUserSelected(final UserKey userKey) {
        return mSelectedUserKeys.contains(userKey);
    }

    public void registerCallback(final Callback callback) {
        if (callback == null) return;
        mCallbacks.add(callback);
    }

    public boolean selectItem(final Object item) {
        return mSelectedItems.add(item);
    }

    public void setAccountKey(final UserKey accountKey) {
        mAccountKey = accountKey;
    }

    public void unregisterCallback(final Callback callback) {
        mCallbacks.remove(callback);
    }

    public boolean deselectItem(final Object item) {
        return mSelectedItems.remove(item);
    }

    private void onItemsCleared() {
        for (final Callback callback : mCallbacks) {
            callback.onItemsCleared();
        }
        mAccountKey = null;
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

    public static UserKey getFirstSelectAccountKey(final List<Object> selectedItems) {
        final Object obj = selectedItems.get(0);
        if (obj instanceof ParcelableUser) {
            final ParcelableUser user = (ParcelableUser) obj;
            return user.account_key;
        } else if (obj instanceof ParcelableStatus) {
            final ParcelableStatus status = (ParcelableStatus) obj;
            return status.account_key;
        }
        return null;
    }

    public static UserKey[] getSelectedUserKeys(final List<Object> selectedItems) {
        final ArrayList<UserKey> userKeys = new ArrayList<>();
        for (final Object item : selectedItems) {
            if (item instanceof ParcelableUser) {
                userKeys.add(((ParcelableUser) item).key);
            } else if (item instanceof ParcelableStatus) {
                userKeys.add(((ParcelableStatus) item).user_key);
            }
        }
        return userKeys.toArray(new UserKey[0]);
    }

    public interface Callback {

        void onItemsCleared();

        void onItemSelected(Object item);

        void onItemUnselected(Object item);

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
                manager.mSelectedUserKeys.add(((ParcelableUser) object).key);
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
            manager.mSelectedUserKeys.clear();
            manager.onItemsCleared();
        }

        @Override
        public boolean remove(final Object object) {
            final boolean ret = super.remove(object);
            if (object instanceof ParcelableStatus) {
                manager.mSelectedStatusIds.remove(((ParcelableStatus) object).id);
            } else if (object instanceof ParcelableUser) {
                manager.mSelectedUserKeys.remove(((ParcelableUser) object).key);
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
