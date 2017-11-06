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

package org.mariotaku.twidere.util

import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskEngine

object PromiseTaskEngine : TaskEngine() {
    override fun <Params : Any?, Result : Any?, Callback : Any?> execute(t: AbstractTask<Params, Result, Callback>?) {
        val dispatcher = getDispatcher<Params, Result, Callback>(t)
        task {
            dispatcher.invokeExecute()
        }.successUi {
            dispatcher.invokeAfterExecute(it)
        }
    }

}
