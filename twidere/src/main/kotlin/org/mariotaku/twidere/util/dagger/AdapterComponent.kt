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

import dagger.Component
import org.mariotaku.twidere.adapter.*
import org.mariotaku.twidere.dagger.module.ApplicationModule
import org.mariotaku.twidere.dagger.module.ChannelModule
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ApplicationModule::class, ChannelModule::class))
interface AdapterComponent {

    fun inject(adapter: DummyItemAdapter)

    fun inject(obj: AccountDetailsAdapter)

    fun inject(obj: ComposeAutoCompleteAdapter)

    fun inject(obj: UserAutoCompleteAdapter)

    companion object : ApplicationContextSingletonHolder<AdapterComponent>(creation@ { application ->
        return@creation DaggerAdapterComponent.builder()
                .applicationModule(ApplicationModule.getInstance(application))
                .channelModule(ChannelModule.getInstance(application))
                .build()
    })
}