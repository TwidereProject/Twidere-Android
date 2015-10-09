/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util.dagger;

import android.support.v7.widget.RecyclerView;

import org.mariotaku.twidere.activity.BasePreferenceActivity;
import org.mariotaku.twidere.activity.BaseThemedActivity;
import org.mariotaku.twidere.activity.support.BaseAppCompatActivity;
import org.mariotaku.twidere.activity.support.ThemedFragmentActivity;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.adapter.BaseArrayAdapter;
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter;
import org.mariotaku.twidere.adapter.DraftsAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.BaseListFragment;
import org.mariotaku.twidere.fragment.BasePreferenceFragment;
import org.mariotaku.twidere.fragment.support.AccountsDashboardFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.provider.TwidereCommandProvider;
import org.mariotaku.twidere.provider.TwidereDataProvider;
import org.mariotaku.twidere.service.BackgroundOperationService;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.task.ManagedAsyncTask;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import dagger.Component;

/**
 * Created by mariotaku on 15/10/5.
 */
@Component(modules = ApplicationModule.class)
public interface GeneralComponent {
    void inject(StatusViewHolder.DummyStatusHolderAdapter object);

    void inject(BaseFragment object);

    void inject(BaseSupportFragment object);

    void inject(MultiSelectEventHandler object);

    void inject(BasePreferenceActivity object);

    void inject(BaseThemedActivity object);

    void inject(BaseSupportDialogFragment object);

    void inject(RefreshService object);

    void inject(ThemedFragmentActivity object);

    void inject(TwidereCommandProvider object);

    void inject(TwidereDataProvider object);

    void inject(BaseListFragment object);

    void inject(BaseAppCompatActivity object);

    void inject(BackgroundOperationService object);

    void inject(BaseRecyclerViewAdapter<RecyclerView.ViewHolder> object);

    void inject(AccountsAdapter object);

    void inject(UserHashtagAutoCompleteAdapter object);

    void inject(AccountsSpinnerAdapter object);

    void inject(BaseArrayAdapter<Object> object);

    void inject(DraftsAdapter object);

    void inject(ManagedAsyncTask<Object, Object, Object> object);

    void inject(BasePreferenceFragment object);

    void inject(BaseDialogFragment object);

    void inject(BaseFiltersFragment.FilteredUsersFragment.FilterUsersListAdapter object);

    void inject(AccountsDashboardFragment.OptionItemsAdapter object);
}
