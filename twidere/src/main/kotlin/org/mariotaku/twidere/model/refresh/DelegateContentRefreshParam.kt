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

package org.mariotaku.twidere.model.refresh

import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.pagination.Pagination


open class DelegateContentRefreshParam(private val delegated: ContentRefreshParam) : ContentRefreshParam {
    override val accountKeys: Array<UserKey>
        get() = delegated.accountKeys

    override val pagination: Array<out Pagination?>?
        get() = delegated.pagination

    override val extraId: Long
        get() = delegated.extraId

    override val tabId: Long
        get() = delegated.tabId

    override val isLoadingMore: Boolean
        get() = delegated.isLoadingMore

    override val shouldAbort: Boolean
        get() = delegated.shouldAbort

    override val isBackground: Boolean
        get() = delegated.isBackground

    override val hasMaxIds: Boolean
        get() = delegated.hasMaxIds
}
