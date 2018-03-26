/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.mariotaku.twidere.model.tab.argument.TabArguments
import org.mariotaku.twidere.model.tab.extra.TabExtras

@Parcelize
data class HomeTab(
        var name: String?,
        var icon: String?,
        var type: String,
        var position: Int,
        var arguments: TabArguments?,
        var extras: TabExtras?
) : Parcelable
