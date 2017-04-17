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

package org.mariotaku.twidere.model.tab.impl

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_MENTIONS_ONLY
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_MY_FOLLOWING_ONLY
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.fragment.InteractionsTimelineFragment
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.model.tab.BooleanHolder
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration
import org.mariotaku.twidere.model.tab.conf.BooleanExtraConfiguration
import org.mariotaku.twidere.model.tab.extra.InteractionsTabExtras
import org.mariotaku.twidere.model.util.AccountUtils

/**
 * Created by mariotaku on 2016/11/27.
 */

class InteractionsTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.interactions)

    override val icon = DrawableHolder.Builtin.NOTIFICATIONS

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_MULTIPLE or TabAccountFlags.FLAG_ACCOUNT_MUTABLE

    override val fragmentClass = InteractionsTimelineFragment::class.java

    override fun getExtraConfigurations(context: Context) = arrayOf(
            BooleanExtraConfiguration(EXTRA_MY_FOLLOWING_ONLY, R.string.following_only, false).mutable(true),
            MentionsOnlyExtraConfiguration(EXTRA_MENTIONS_ONLY).mutable(true)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as InteractionsTabExtras
        when (extraConf.key) {
            EXTRA_MY_FOLLOWING_ONLY -> {
                extras.isMyFollowingOnly = (extraConf as BooleanExtraConfiguration).value
            }
            EXTRA_MENTIONS_ONLY -> {
                extras.isMentionsOnly = (extraConf as BooleanExtraConfiguration).value
            }
        }
        return true
    }

    override fun readExtraConfigurationFrom(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as? InteractionsTabExtras ?: return false
        when (extraConf.key) {
            EXTRA_MY_FOLLOWING_ONLY -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isMyFollowingOnly
            }
            EXTRA_MENTIONS_ONLY -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isMentionsOnly
            }
        }
        return true
    }

    private class MentionsOnlyExtraConfiguration(key: String) : BooleanExtraConfiguration(key,
            StringHolder.resource(R.string.mentions_only),
            MentionsOnlyExtraConfiguration.HasOfficialBooleanHolder()) {

        private var valueBackup: Boolean = false

        override fun onAccountSelectionChanged(account: AccountDetails?) {
            val hasOfficial: Boolean
            if (account == null || account.dummy) {
                hasOfficial = AccountUtils.hasOfficialKeyAccount(context)
            } else {
                hasOfficial = account.isOfficial(context)
            }
            (defaultValue as HasOfficialBooleanHolder).hasOfficial = hasOfficial
            val checkBox = view.findViewById(android.R.id.checkbox) as CheckBox
            val titleView = view.findViewById(android.R.id.title) as TextView
            val summaryView = view.findViewById(android.R.id.summary) as TextView
            view.isEnabled = hasOfficial
            titleView.isEnabled = hasOfficial
            summaryView.isEnabled = hasOfficial
            checkBox.isEnabled = hasOfficial
            if (hasOfficial) {
                checkBox.isChecked = valueBackup
                summaryView.visibility = View.GONE
            } else {
                valueBackup = checkBox.isChecked
                checkBox.isChecked = true
                summaryView.setText(R.string.summary_interactions_not_available)
                summaryView.visibility = View.VISIBLE
            }
        }

        override var value: Boolean
            get() {
                if ((defaultValue as HasOfficialBooleanHolder).hasOfficial) {
                    return super.value
                }
                return valueBackup
            }
            set(value) {
                super.value = value
            }

        private class HasOfficialBooleanHolder : BooleanHolder() {

            var hasOfficial: Boolean = false

            override fun createBoolean(context: Context): Boolean {
                return hasOfficial
            }

        }
    }

}
