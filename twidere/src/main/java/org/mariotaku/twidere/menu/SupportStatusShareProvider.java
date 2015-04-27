/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.menu;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableStatus;

import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.createStatusShareIntent;

/**
 * Created by mariotaku on 14/12/7.
 */
public class SupportStatusShareProvider extends ActionProvider implements Constants {

    private final Context mContext;
    private ParcelableStatus mStatus;

    public SupportStatusShareProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public View onCreateActionView(MenuItem forItem) {
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
    public void onPrepareSubMenu(SubMenu subMenu) {
        final ParcelableStatus status = mStatus;
        if (status == null) return;
        final Intent shareIntent = createStatusShareIntent(mContext, status);
        subMenu.removeGroup(MENU_GROUP_STATUS_SHARE);
        addIntentToMenu(mContext, subMenu, shareIntent, MENU_GROUP_STATUS_SHARE);
    }

    public void setStatus(ParcelableStatus status) {
        mStatus = status;
    }
}
