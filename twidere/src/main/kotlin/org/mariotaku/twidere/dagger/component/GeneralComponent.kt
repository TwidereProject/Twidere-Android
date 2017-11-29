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

package org.mariotaku.twidere.dagger.component

import android.support.v7.widget.RecyclerView
import dagger.Component
import org.mariotaku.twidere.activity.*
import org.mariotaku.twidere.adapter.*
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.dagger.DependencyHolder
import org.mariotaku.twidere.dagger.module.ApplicationModule
import org.mariotaku.twidere.dagger.module.ChannelModule
import org.mariotaku.twidere.data.impl.UserLiveData
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.fragment.BaseDialogFragment
import org.mariotaku.twidere.fragment.BaseFragment
import org.mariotaku.twidere.fragment.BasePreferenceFragment
import org.mariotaku.twidere.fragment.filter.FilteredUsersFragment
import org.mariotaku.twidere.fragment.media.ExoPlayerPageFragment
import org.mariotaku.twidere.fragment.media.VideoPageFragment
import org.mariotaku.twidere.fragment.preference.ThemedPreferenceDialogFragmentCompat
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.loader.DefaultAPIConfigLoader
import org.mariotaku.twidere.loader.ParcelableStatusLoader
import org.mariotaku.twidere.loader.statuses.AbsRequestStatusesLoader
import org.mariotaku.twidere.loader.userlists.BaseUserListsLoader
import org.mariotaku.twidere.preference.AccountsListPreference
import org.mariotaku.twidere.preference.KeyboardShortcutPreference
import org.mariotaku.twidere.preference.PremiumEntryPreference
import org.mariotaku.twidere.preference.PremiumEntryPreferenceCategory
import org.mariotaku.twidere.preference.sync.SyncItemPreference
import org.mariotaku.twidere.promise.*
import org.mariotaku.twidere.provider.CacheProvider
import org.mariotaku.twidere.provider.TwidereDataProvider
import org.mariotaku.twidere.service.*
import org.mariotaku.twidere.task.BaseAbstractTask
import org.mariotaku.twidere.text.util.EmojiEditableFactory
import org.mariotaku.twidere.text.util.EmojiSpannableFactory
import org.mariotaku.twidere.util.MultiSelectEventHandler
import org.mariotaku.twidere.util.filter.UrlFiltersSubscriptionProvider
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import org.mariotaku.twidere.util.sync.SyncTaskRunner
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, ChannelModule::class])
interface GeneralComponent {
    fun inject(application: TwidereApplication)

    fun inject(obj: MultiSelectEventHandler)

    fun inject(obj: LegacyTaskService)

    fun inject(obj: ComposeActivity)

    fun inject(obj: TwidereDataProvider)

    fun inject(obj: BaseActivity)

    fun inject(obj: BaseRecyclerViewAdapter<RecyclerView.ViewHolder>)

    fun inject(obj: AccountsSpinnerAdapter)

    fun inject(obj: BaseArrayAdapter<Any>)

    fun inject(obj: DraftsAdapter)

    fun inject(obj: FilteredUsersFragment.FilterUsersListAdapter)

    fun inject(obj: EmojiSpannableFactory)

    fun inject(obj: EmojiEditableFactory)

    fun inject(obj: AccountsListPreference.AccountItemPreference)

    fun inject(obj: DependencyHolder)

    fun inject(provider: CacheProvider)

    fun inject(loader: AbsRequestStatusesLoader)

    fun inject(activity: MediaViewerActivity)

    fun inject(service: JobTaskService)

    fun inject(task: BaseAbstractTask<Any, Any, Any>)

    fun inject(preference: KeyboardShortcutPreference)

    fun inject(loader: ParcelableStatusLoader)

    fun inject(loader: DefaultAPIConfigLoader)

    fun inject(service: BaseIntentService)

    fun inject(runner: SyncTaskRunner)

    fun inject(preference: SyncItemPreference)

    fun inject(provider: UrlFiltersSubscriptionProvider)

    fun inject(preference: PremiumEntryPreference)

    fun inject(preference: PremiumEntryPreferenceCategory)

    fun inject(loader: CacheUserSearchLoader)

    fun inject(loader: BaseUserListsLoader)

    fun inject(controller: PremiumDashboardActivity.BaseItemViewController)

    fun inject(service: StreamingService)

    fun inject(service: BaseService)

    fun inject(activity: MainActivity)

    fun inject(promises: MessagePromises)

    fun inject(promises: StatusPromises)

    fun inject(promises: FriendshipPromises)

    fun inject(promises: BlockPromises)

    fun inject(promises: MutePromises)

    fun inject(promises: DefaultFeaturesPromises)

    fun inject(promises: LaunchPresentationsPromises)

    fun inject(promise: UpdateStatusPromise)

    fun inject(promise: GetTrendsPromise)

    fun inject(fragment: BaseFragment)

    fun inject(fragment: BaseDialogFragment)

    fun inject(fragment: BasePreferenceFragment)

    fun inject(fragment: ExoPlayerPageFragment)

    fun inject(fragment: VideoPageFragment)

    fun inject(fragment: ThemedPreferenceDialogFragmentCompat)

    fun inject(adapter: DummyItemAdapter)

    fun inject(obj: AccountDetailsAdapter)

    fun inject(obj: ComposeAutoCompleteAdapter)

    fun inject(obj: UserAutoCompleteAdapter)

    fun inject(promises: UserListPromises)

    fun inject(promises: SavedSearchPromises)

    fun inject(promises: RefreshPromises)

    fun inject(liveData: UserLiveData)

    fun inject(adapter: AccountSelectorAdapter)

    companion object : ApplicationContextSingletonHolder<GeneralComponent>(creation@ { application ->
        return@creation DaggerGeneralComponent.builder()
                .applicationModule(ApplicationModule.get(application))
                .channelModule(ChannelModule.get(application))
                .build()
    })
}
