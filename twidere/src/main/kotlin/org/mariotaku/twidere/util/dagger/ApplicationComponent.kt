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

package org.mariotaku.twidere.util.dagger

import android.support.v7.widget.RecyclerView
import dagger.Component
import org.mariotaku.twidere.activity.*
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter
import org.mariotaku.twidere.adapter.BaseArrayAdapter
import org.mariotaku.twidere.adapter.BaseRecyclerViewAdapter
import org.mariotaku.twidere.adapter.DraftsAdapter
import org.mariotaku.twidere.app.TwidereApplication
import org.mariotaku.twidere.dagger.module.ApplicationModule
import org.mariotaku.twidere.dagger.module.ChannelModule
import org.mariotaku.twidere.fragment.filter.FilteredUsersFragment
import org.mariotaku.twidere.loader.CacheUserSearchLoader
import org.mariotaku.twidere.loader.DefaultAPIConfigLoader
import org.mariotaku.twidere.loader.ParcelableStatusLoader
import org.mariotaku.twidere.loader.ParcelableUserLoader
import org.mariotaku.twidere.loader.statuses.AbsRequestStatusesLoader
import org.mariotaku.twidere.loader.userlists.BaseUserListsLoader
import org.mariotaku.twidere.preference.AccountsListPreference
import org.mariotaku.twidere.preference.KeyboardShortcutPreference
import org.mariotaku.twidere.preference.PremiumEntryPreference
import org.mariotaku.twidere.preference.PremiumEntryPreferenceCategory
import org.mariotaku.twidere.preference.sync.SyncItemPreference
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
@Component(modules = arrayOf(ApplicationModule::class, ChannelModule::class))
interface ApplicationComponent {

    fun inject(application: TwidereApplication)

    companion object : ApplicationContextSingletonHolder<ApplicationComponent>(creation@ { application ->
        return@creation DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule.getInstance(application))
                .channelModule(ChannelModule.getInstance(application))
                .build()
    })
}
