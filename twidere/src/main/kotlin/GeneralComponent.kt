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

package org.mariotaku.twidere.util.dagger

import android.support.v7.widget.RecyclerView
import dagger.Component
import org.mariotaku.twidere.activity.BaseActivity
import org.mariotaku.twidere.activity.ComposeActivity
import org.mariotaku.twidere.activity.MediaViewerActivity
import org.mariotaku.twidere.activity.iface.APIEditorActivity
import org.mariotaku.twidere.adapter.*
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.fragment.*
import org.mariotaku.twidere.loader.MicroBlogAPIStatusesLoader
import org.mariotaku.twidere.loader.ParcelableStatusLoader
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.preference.AccountsListPreference
import org.mariotaku.twidere.preference.KeyboardShortcutPreference
import org.mariotaku.twidere.provider.CacheProvider
import org.mariotaku.twidere.provider.TwidereDataProvider
import org.mariotaku.twidere.service.BackgroundOperationService
import org.mariotaku.twidere.service.RefreshService
import org.mariotaku.twidere.task.*
import org.mariotaku.twidere.task.twitter.GetActivitiesTask
import org.mariotaku.twidere.task.twitter.GetStatusesTask
import org.mariotaku.twidere.task.twitter.UpdateStatusTask
import org.mariotaku.twidere.text.util.EmojiEditableFactory
import org.mariotaku.twidere.text.util.EmojiSpannableFactory
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.MultiSelectEventHandler
import javax.inject.Singleton

/**
 * Created by mariotaku on 15/10/5.
 */
@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface GeneralComponent {
    fun inject(adapter: DummyItemAdapter)

    fun inject(obj: BaseSupportFragment)

    fun inject(obj: MultiSelectEventHandler)

    fun inject(obj: BaseDialogFragment)

    fun inject(obj: RefreshService)

    fun inject(obj: ComposeActivity)

    fun inject(obj: TwidereDataProvider)

    fun inject(obj: BaseListFragment)

    fun inject(obj: BaseActivity)

    fun inject(obj: BackgroundOperationService)

    fun inject(obj: BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)

    fun inject(obj: AccountsAdapter)

    fun inject(obj: ComposeAutoCompleteAdapter)

    fun inject(obj: UserAutoCompleteAdapter)

    fun inject(obj: AccountsSpinnerAdapter)

    fun inject(obj: BaseArrayAdapter<Any>)

    fun inject(obj: DraftsAdapter)

    fun inject(obj: ManagedAsyncTask<Any, Any, Any>)

    fun inject(obj: BasePreferenceFragment)

    fun inject(obj: BaseFiltersFragment.FilteredUsersFragment.FilterUsersListAdapter)

    fun inject(obj: EmojiSpannableFactory)

    fun inject(obj: EmojiEditableFactory)

    fun inject(obj: AccountsListPreference.AccountItemPreference)

    fun inject(obj: MessagesConversationFragment.SetReadStateTask)

    fun inject(obj: DependencyHolder)

    fun inject(provider: CacheProvider)

    fun inject(loader: MicroBlogAPIStatusesLoader)

    fun inject(activity: MediaViewerActivity)

    fun inject(task: GetStatusesTask)

    fun inject(task: GetActivitiesTask)

    fun inject(task: GetDirectMessagesTask)

    fun inject(task: AbsFriendshipOperationTask)

    fun inject(preference: KeyboardShortcutPreference)

    fun inject(loader: ParcelableUserLoader)

    fun inject(loader: ParcelableStatusLoader)

    fun inject(task: GetTrendsTask)

    fun inject(task: UpdateProfileBackgroundImageTask<Any>)

    fun inject(task: UpdateProfileBannerImageTask<Any>)

    fun inject(task: AsyncTwitterWrapper.UpdateProfileImageTask<Any>)

    fun inject(loader: APIEditorActivity.LoadDefaultsChooserDialogFragment.DefaultAPIConfigLoader)

    fun inject(task: UpdateStatusTask)

    fun inject(application: TwidereApplication)
}
