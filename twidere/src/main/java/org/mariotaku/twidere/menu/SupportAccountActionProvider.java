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
import android.support.v4.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableAccount;

public class SupportAccountActionProvider extends ActionProvider implements TwidereConstants {

    public static final int MENU_GROUP = 201;

    private ParcelableAccount[] mAccounts;

    private long[] mAccountIds;
    private boolean mExclusive;

    public SupportAccountActionProvider(final Context context, final ParcelableAccount[] accounts) {
        super(context);
        setAccounts(accounts);
    }

    public SupportAccountActionProvider(final Context context) {
        this(context, ParcelableAccount.getAccounts(context, false, false));
    }

    public ParcelableAccount[] getAccounts() {
        return mAccounts;
    }

    @Override
    public boolean onPerformDefaultAction() {
        return true;
    }

    public boolean isExclusive() {
        return mExclusive;
    }


    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    public void setAccounts(ParcelableAccount[] accounts) {
        mAccounts = accounts;
    }

    @Override
    public void onPrepareSubMenu(final SubMenu subMenu) {
        subMenu.removeGroup(MENU_GROUP);
        if (mAccounts == null) return;
        for (int i = 0, j = mAccounts.length; i < j; i++) {
            final ParcelableAccount account = mAccounts[i];
            final MenuItem item = subMenu.add(MENU_GROUP, Menu.NONE, i, account.name);
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_ACCOUNT, account);
            item.setIntent(intent);
        }
        subMenu.setGroupCheckable(MENU_GROUP, true, mExclusive);
        if (mAccountIds == null) return;
        for (int i = 0, j = subMenu.size(); i < j; i++) {
            final MenuItem item = subMenu.getItem(i);
            if (ArrayUtils.contains(mAccountIds, mAccounts[i].account_id)) {
                item.setChecked(true);
            }
        }
    }

    public void setExclusive(boolean exclusive) {
        mExclusive = exclusive;
    }

    public void setSelectedAccountIds(final long... accountIds) {
        mAccountIds = accountIds;
    }

}
