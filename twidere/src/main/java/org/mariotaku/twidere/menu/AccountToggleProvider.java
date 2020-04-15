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

package org.mariotaku.twidere.menu;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.AccountDetails;
import org.mariotaku.twidere.model.UserKey;

public class AccountToggleProvider extends ActionProvider implements TwidereConstants {

    public static final int MENU_GROUP = 201;

    private AccountDetails[] mAccounts;

    private boolean mExclusive;

    public AccountToggleProvider(final Context context) {
        super(context);
    }

    public AccountDetails[] getAccounts() {
        return mAccounts;
    }

    public void setAccounts(AccountDetails[] accounts) {
        mAccounts = accounts;
    }

    @NonNull
    public UserKey[] getActivatedAccountKeys() {
        if (mAccounts == null) return new UserKey[0];
        UserKey[] temp = new UserKey[mAccounts.length];
        int len = 0;
        for (AccountDetails account : mAccounts) {
            if (account.activated) {
                temp[len++] = account.key;
            }
        }
        final UserKey[] result = new UserKey[len];
        System.arraycopy(temp, 0, result, 0, len);
        return result;
    }

    public boolean isExclusive() {
        return mExclusive;
    }

    public void setExclusive(boolean exclusive) {
        mExclusive = exclusive;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public boolean onPerformDefaultAction() {
        return true;
    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public void onPrepareSubMenu(final SubMenu subMenu) {
        subMenu.removeGroup(MENU_GROUP);
        if (mAccounts == null) return;
        for (int i = 0, j = mAccounts.length; i < j; i++) {
            final AccountDetails account = mAccounts[i];
            final MenuItem item = subMenu.add(MENU_GROUP, Menu.NONE, i, account.user.name);
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_ACCOUNT, account);
            item.setIntent(intent);
        }
        subMenu.setGroupCheckable(MENU_GROUP, true, mExclusive);
        for (int i = 0, j = subMenu.size(); i < j; i++) {
            final MenuItem item = subMenu.getItem(i);
            if (mAccounts[i].activated) {
                item.setChecked(true);
            }
        }
    }

    public void setAccountActivated(@NonNull UserKey accountKey, boolean isChecked) {
        if (mAccounts == null) return;
        for (final AccountDetails account : mAccounts) {
            if (accountKey.equals(account.key)) {
                account.activated = isChecked;
            }
        }
    }
}
