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

import dagger.Component
import org.mariotaku.twidere.dagger.module.ChannelModule
import org.mariotaku.twidere.dagger.module.GeneralModule
import org.mariotaku.twidere.promise.*
import org.mariotaku.twidere.util.lang.ApplicationContextSingletonHolder
import javax.inject.Singleton


@Singleton
@Component(modules = arrayOf(GeneralModule::class, ChannelModule::class))
interface PromisesComponent {

    fun inject(promises: MessagePromises)

    fun inject(promises: StatusPromises)

    fun inject(promises: FriendshipPromises)

    fun inject(promises: BlockPromises)

    fun inject(promises: MutePromises)

    fun inject(promises: DefaultFeaturesPromises)

    companion object : ApplicationContextSingletonHolder<PromisesComponent>(creation@ { application ->
        return@creation DaggerPromisesComponent.builder()
                .generalModule(GeneralModule.getInstance(application))
                .channelModule(ChannelModule.getInstance(application))
                .build()
    })
}