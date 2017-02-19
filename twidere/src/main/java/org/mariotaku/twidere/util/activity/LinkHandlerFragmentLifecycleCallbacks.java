/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.mariotaku.twidere.activity.LinkHandlerActivity;
import org.mariotaku.twidere.fragment.iface.IToolBarSupportFragment;

/**
 * Created by mariotaku on 2017/2/19.
 */

public class LinkHandlerFragmentLifecycleCallbacks {

    public static FragmentManager.FragmentLifecycleCallbacks get(final LinkHandlerActivity activity) {
        final FragmentManager fm = activity.getSupportFragmentManager();
        return fm.new FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(final FragmentManager fm1, final Fragment f, final View v, final Bundle savedInstanceState) {
                if (f instanceof IToolBarSupportFragment) {
                    activity.setSupportActionBar(((IToolBarSupportFragment) f).getToolbar());
                }
            }
        };
    }
}
