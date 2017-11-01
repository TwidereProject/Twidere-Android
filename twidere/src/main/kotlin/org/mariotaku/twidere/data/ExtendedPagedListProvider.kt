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

package org.mariotaku.twidere.data

import android.arch.paging.DataSource
import android.arch.paging.LivePagedListProvider
import org.mariotaku.ktextension.weak

abstract class ExtendedPagedListProvider<Key, Value> : LivePagedListProvider<Key, Value>() {

    private val dataControllers: MutableList<DataControllerImpl> = ArrayList()

    override final fun createDataSource(): DataSource<Key, Value> {
        val source = onCreateDataSource()
        dataControllers.forEach { it.attach(source) }
        return source
    }

    fun obtainDataController(): DataController {
        val controller = ExtendedPagedListProvider.DataControllerImpl()
        dataControllers.add(controller)
        return controller
    }

    protected abstract fun onCreateDataSource(): DataSource<Key, Value>

    interface DataController {
        fun invalidate(): Boolean
    }

    private class DataControllerImpl : DataController {
        private var dataSource: DataSource<*, *>? by weak()

        override fun invalidate(): Boolean {
            val src = dataSource ?: return false
            src.invalidate()
            return true
        }

        fun attach(source: DataSource<*, *>) {
            dataSource = source
        }

    }

}