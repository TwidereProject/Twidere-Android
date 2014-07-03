package org.mariotaku.twidere.menu;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.Account;

public class AccountActionProvider extends ActionProvider implements TwidereConstants {

	public static final int MENU_GROUP = 201;

	private final Account[] mAccounts;

	private long mAccountId;

	public AccountActionProvider(final Context context) {
		super(context);
		mAccounts = Account.getAccounts(context, false, false);
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

	@Override
	public View onCreateActionView() {
		return null;
	}

	@Override
	public void onPrepareSubMenu(final SubMenu subMenu) {
		subMenu.removeGroup(MENU_GROUP);
		for (final Account account : mAccounts) {
			final MenuItem item = subMenu.add(MENU_GROUP, Menu.NONE, 0, account.name);
			final Intent intent = new Intent();
			intent.putExtra(EXTRA_ACCOUNT, account);
			item.setIntent(intent);
		}
		subMenu.setGroupCheckable(MENU_GROUP, true, true);
		for (int i = 0, j = subMenu.size(); i < j; i++) {
			final MenuItem item = subMenu.getItem(i);
			final Intent intent = item.getIntent();
			final Account account = intent.getParcelableExtra(EXTRA_ACCOUNT);
			if (account.account_id == mAccountId) {
				item.setChecked(true);
			}
		}
	}

	public void setAccountId(final long accountId) {
		mAccountId = accountId;
	}

}
