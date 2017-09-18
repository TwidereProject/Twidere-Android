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

package org.mariotaku.twidere.util.promotion

import android.content.Context
import android.content.SharedPreferences
import android.view.ViewGroup
import java.util.*

/**
 * Created by mariotaku on 2017/9/17.
 */
abstract class PromotionService {

    protected lateinit var context: Context
        private set
    protected lateinit var preferences: SharedPreferences
        private set

    abstract fun appStarted()

    abstract fun setupBanner(container: ViewGroup, type: BannerType, params: ViewGroup.LayoutParams? = null)

    abstract fun loadBanner(container: ViewGroup, extras: BannerExtras? = null)

    protected open fun init(context: Context) {

    }

    companion object {

        fun newInstance(context: Context, preferences: SharedPreferences): PromotionService {
            val instance = ServiceLoader.load(PromotionService::class.java).firstOrNull() ?: run {
                return@run DummyPromotionService()
            }
            instance.context = context
            instance.preferences = preferences
            instance.init(context)
            return instance
        }

    }

    enum class BannerType {
        PREMIUM_DASHBOARD, QUICK_SEARCH, MEDIA_PAUSE
    }

    data class BannerExtras(val contentUrl: String?)
}