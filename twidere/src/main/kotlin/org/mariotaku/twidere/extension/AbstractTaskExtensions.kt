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

package org.mariotaku.twidere.extension

import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.ManualTaskStarter

fun <P, R, C> AbstractTask<P, R, C>.promise(): Promise<R, Exception> {
    return promiseOnUi {
        ManualTaskStarter.invokeBeforeExecute(this)
    }.then {
        return@then ManualTaskStarter.invokeExecute(this)
    }.successUi { result ->
        ManualTaskStarter.invokeAfterExecute(this, result)
    }
}