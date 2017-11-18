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

package org.mariotaku.twidere.dagger.module

import android.app.Application
import dagger.Module
import dagger.Provides
import org.mariotaku.twidere.util.MapFragmentFactory
import org.mariotaku.twidere.util.OSMMapFragmentFactory
import org.mariotaku.twidere.util.gifshare.GifShareProvider
import org.mariotaku.twidere.util.gifshare.NullGifShareProvider
import org.mariotaku.twidere.util.lang.SingletonHolder
import org.mariotaku.twidere.util.premium.DummyExtraFeaturesService
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.promotion.DummyPromotionService
import org.mariotaku.twidere.util.promotion.PromotionService
import org.mariotaku.twidere.util.sync.DataSyncProvider
import org.mariotaku.twidere.util.sync.OpenSourceSyncProviderInfoFactory
import javax.inject.Singleton

@Module
class ChannelModule private constructor(private val application: Application) {

    @Provides
    @Singleton
    fun promotionService(): PromotionService {
        return DummyPromotionService()
    }

    @Provides
    @Singleton
    fun mapFragmentFactory(): MapFragmentFactory {
        return OSMMapFragmentFactory
    }


    @Provides
    @Singleton
    fun gifShareProvider(): GifShareProvider {
        return NullGifShareProvider()
    }

    @Provides
    @Singleton
    fun extraFeaturesService(): ExtraFeaturesService {
        return DummyExtraFeaturesService()
    }

    @Provides
    @Singleton
    fun dataSyncProviderFactory(): DataSyncProvider.Factory {
        return OpenSourceSyncProviderInfoFactory()
    }

    companion object : SingletonHolder<ChannelModule, Application>(::ChannelModule)
}
