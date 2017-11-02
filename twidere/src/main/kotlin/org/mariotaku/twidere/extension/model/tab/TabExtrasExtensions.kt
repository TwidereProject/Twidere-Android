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

package org.mariotaku.twidere.extension.model.tab

import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.model.tab.extra.HomeTabExtras
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses

fun HomeTabExtras.applyToSelection(expressions: MutableList<Expression>, expressionArgs: MutableList<String>) {
    if (isHideRetweets) {
        expressions.add(Expression.equalsArgs(Statuses.IS_RETWEET))
        expressionArgs.add("0")
    }
    if (isHideQuotes) {
        expressions.add(Expression.equalsArgs(Statuses.IS_QUOTE))
        expressionArgs.add("0")
    }
    if (isHideReplies) {
        expressions.add(Expression.isNull(Columns.Column(Statuses.IN_REPLY_TO_STATUS_ID)))
    }
}