package org.mariotaku.twidere.extension.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Xml
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.FiltersData
import org.mariotaku.twidere.model.UserKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Created by mariotaku on 2016/12/29.
 */

@RunWith(AndroidJUnit4::class)
class FiltersDataExtensionsTest {
    @Test
    fun testXmlSerialization() {
        val filters = FiltersData().apply {
            users = listOf(userItem(UserKey("123456", "twitter.com"), "name", "screen_name"))
            links = listOf(baseItem("twitter.com"))
            keywords = listOf(baseItem("Keyword"))
            sources = listOf(baseItem("Spam Client"))
        }
        val serializer = Xml.newSerializer()
        val baos = ByteArrayOutputStream()
        serializer.setOutput(baos, "UTF-8")
        filters.serialize(serializer)
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(baos.toByteArray()), "UTF-8")
        val newFilters = FiltersData()
        newFilters.parse(parser)

        Assert.assertEquals(filters.users, newFilters.users)
        Assert.assertEquals(filters.keywords, newFilters.keywords)
        Assert.assertEquals(filters.sources, newFilters.sources)
        Assert.assertEquals(filters.links, newFilters.links)
    }

    private fun baseItem(value: String): FiltersData.BaseItem {
        return FiltersData.BaseItem().apply {
            this.value = value
        }
    }

    private fun userItem(key: UserKey, name: String, screenName: String): FiltersData.UserItem {
        return FiltersData.UserItem().apply {
            this.userKey = key
            this.name = name
            this.screenName = screenName
        }
    }
}

