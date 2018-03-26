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

package org.mariotaku.twidere.extension.model

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.extension.queryAll
import org.mariotaku.twidere.model.Tab
import org.mariotaku.twidere.provider.TwidereDataStore.Tabs
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.util.*

@RunWith(AndroidJUnit4::class)
class HomeTabExtensionsTest {
    @Test
    fun testSerialize() {
        val context = InstrumentationRegistry.getTargetContext()
        val legacyTabs = context.contentResolver.queryAll(Tabs.CONTENT_URI, Tabs.COLUMNS,
                null, null, Tabs.POSITION, cls = Tab::class.java)!!
        val tabs = legacyTabs.map { it.toHomeTab() }

        File(context.cacheDir, "${UUID.randomUUID()}.xml").writer(Charsets.UTF_8).use {
            val serializer = XmlPullParserFactory.newInstance().newSerializer()
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.setOutput(it)
            serializer.startDocument("UTF-8", true)
            serializer.startTag(null, "tabs")
            tabs.forEach { it.serialize(serializer) }
            serializer.endTag(null, "tabs")
            serializer.endDocument()
            it.flush()
        }
    }
}