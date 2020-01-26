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

package org.mariotaku.twidere.model.tab

import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.fragment.CustomTabsFragment.TabEditorDialogFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.tab.impl.*

/**
 * Created by mariotaku on 2016/11/27.
 */

abstract class TabConfiguration {

    abstract val name: StringHolder

    abstract val icon: DrawableHolder

    @get:TabAccountFlags
    abstract val accountFlags: Int

    abstract val fragmentClass: Class<out Fragment>

    open val isSingleTab: Boolean
        get() = false

    open val sortPosition: Int
        get() = 0

    open fun getExtraConfigurations(context: Context): Array<ExtraConfiguration>? {
        return null
    }

    open fun applyExtraConfigurationTo(tab: Tab, extraConf: ExtraConfiguration): Boolean {
        return true
    }

    open fun readExtraConfigurationFrom(tab: Tab, extraConf: ExtraConfiguration): Boolean {
        return false
    }

    open fun checkAccountAvailability(details: AccountDetails): Boolean {
        return true
    }

    abstract class ExtraConfiguration {
        val key: String
        val title: StringHolder

        var summary: StringHolder? = null
        var headerTitle: StringHolder? = null

        var position: Int = 0
        var isMutable: Boolean = false

        lateinit var context: Context
            private set

        lateinit var view: View
            private set

        protected constructor(key: String, title: StringHolder) {
            this.key = key
            this.title = title
        }

        protected constructor(key: String, titleRes: Int) :this(key, StringHolder.resource(titleRes))

        fun summary(summary: StringHolder): ExtraConfiguration {
            this.summary = summary
            return this
        }

        fun summary(@StringRes summaryRes: Int): ExtraConfiguration {
            summary = StringHolder.resource(summaryRes)
            return this
        }

        fun headerTitle(title: StringHolder?): ExtraConfiguration {
            headerTitle = title
            return this
        }

        fun headerTitle(@StringRes titleRes: Int): ExtraConfiguration {
            headerTitle = StringHolder.resource(titleRes)
            return this
        }

        fun mutable(mutable: Boolean): ExtraConfiguration {
            isMutable = mutable
            return this
        }

        abstract fun onCreateView(context: Context, parent: ViewGroup): View

        @CallSuper
        open fun onCreate(context: Context) {
            this.context = context
        }

        @CallSuper
        open fun onViewCreated(context: Context, view: View, fragment: TabEditorDialogFragment) {
            this.view = view
        }

        open fun onActivityResult(fragment: TabEditorDialogFragment, requestCode: Int,
                resultCode: Int, data: Intent?) {

        }

        open fun onAccountSelectionChanged(account: AccountDetails?) {

        }

        open fun showRequiredError() {
            val titleString = title.createString(context)
            Toast.makeText(context, context.getString(R.string.message_tab_field_is_required,
                    titleString), Toast.LENGTH_SHORT).show()
        }
    }

    companion object {

        fun all(): List<Pair<String, TabConfiguration>> {
            return allTypes().mapNotNull {
                val conf = ofType(it) ?: return@mapNotNull null
                return@mapNotNull Pair(it, conf)
            }
        }

        fun allTypes(): Array<String> {
            return arrayOf(CustomTabType.HOME_TIMELINE, CustomTabType.NOTIFICATIONS_TIMELINE,
                    CustomTabType.TRENDS_SUGGESTIONS, CustomTabType.DIRECT_MESSAGES,
                    CustomTabType.FAVORITES, CustomTabType.USER_TIMELINE,
                    CustomTabType.SEARCH_STATUSES, CustomTabType.LIST_TIMELINE,
                    CustomTabType.PUBLIC_TIMELINE, CustomTabType.NETWORK_PUBLIC_TIMELINE)
        }

        fun ofType(@CustomTabType type: String): TabConfiguration? {
            when (type) {
                CustomTabType.HOME_TIMELINE -> return HomeTabConfiguration()
                CustomTabType.NOTIFICATIONS_TIMELINE -> return InteractionsTabConfiguration()
                CustomTabType.DIRECT_MESSAGES -> return MessagesTabConfiguration()
                CustomTabType.LIST_TIMELINE -> return UserListTimelineTabConfiguration()
                CustomTabType.FAVORITES -> return FavoriteTimelineTabConfiguration()
                CustomTabType.USER_TIMELINE -> return UserTimelineTabConfiguration()
                CustomTabType.TRENDS_SUGGESTIONS -> return TrendsTabConfiguration()
                CustomTabType.SEARCH_STATUSES -> return SearchTabConfiguration()
                CustomTabType.PUBLIC_TIMELINE -> return PublicTimelineTabConfiguration()
                CustomTabType.NETWORK_PUBLIC_TIMELINE -> return NetworkPublicTimelineTabConfiguration()
            }
            return null
        }
    }

}
