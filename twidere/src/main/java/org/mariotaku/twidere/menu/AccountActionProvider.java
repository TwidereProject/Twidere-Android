package org.mariotaku.twidere.menu;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.ParcelableAccount;

public class AccountActionProvider extends ActionProvider implements TwidereConstants {

    public static final int MENU_GROUP = 201;

    private ParcelableAccount[] mAccounts;

    private long[] mAccountIds;
    private boolean mExclusive;

    public AccountActionProvider(final Context context, final ParcelableAccount[] accounts) {
        super(context);
        setAccounts(accounts);
    }

    public AccountActionProvider(final Context context) {
        this(context, ParcelableAccount.getAccounts(context, false, false));
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
            final Intent intent = item.getIntent();
            final ParcelableAccount account = intent.getParcelableExtra(EXTRA_ACCOUNT);
            if (ArrayUtils.contains(mAccountIds, account.account_id)) {
                item.setChecked(true);
            }
        }
    }

    public boolean isExclusive() {
        return mExclusive;
    }


    public void setExclusive(boolean exclusive) {
        mExclusive = exclusive;
    }

    public void setSelectedAccountIds(final long... accountIds) {
        mAccountIds = accountIds;
    }

}
