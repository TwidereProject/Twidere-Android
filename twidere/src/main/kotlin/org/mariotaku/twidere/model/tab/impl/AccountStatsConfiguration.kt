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

import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.TabAccountFlags
import org.mariotaku.twidere.fragment.stats.AccountStatsFragment
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.model.tab.StringHolder
import org.mariotaku.twidere.model.tab.TabConfiguration

class AccountStatsConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.title_account_stats)

    override val icon = DrawableHolder.Builtin.TRENDS

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_REQUIRED or TabAccountFlags.FLAG_ACCOUNT_MUTABLE

    override val fragmentClass = AccountStatsFragment::class.java

}
