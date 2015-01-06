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

package org.mariotaku.twidere.activity.support;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Spinner;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.model.ParcelableAccount;
import org.mariotaku.twidere.util.ThemeUtils;

import java.util.List;

/**
 * Created by mariotaku on 15/1/6.
 */
public class GlobalSearchBoxActivity extends BaseSupportActivity {

    private Spinner mAccountSpinner;

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getGlobalSearchThemeResource(this);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_search_box);
        final List<ParcelableAccount> accounts = ParcelableAccount.getAccountsList(this, false);
        final AccountsSpinnerAdapter accountsSpinnerAdapter = new AccountsSpinnerAdapter(this, R.layout.spinner_item_account_icon);
        accountsSpinnerAdapter.setDropDownViewResource(R.layout.list_item_user);
        accountsSpinnerAdapter.addAll(accounts);
        mAccountSpinner.setAdapter(accountsSpinnerAdapter);
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final int index = accountsSpinnerAdapter.findItemPosition(intent.getLongExtra(EXTRA_ACCOUNT_ID, -1));
            if (index != -1) {
                mAccountSpinner.setSelection(index);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateWindowAttributes();
    }

    private void updateWindowAttributes() {
        final Window window = getWindow();
        final WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        window.setAttributes(attributes);
    }

}
