package org.mariotaku.twidere.util.filter

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2017/1/11.
 */
@RunWith(AndroidJUnit4::class)
class UrlFiltersSubscriptionProviderTest {
    @Test
    fun testFetchXml() {
        val context = InstrumentationRegistry.getTargetContext()
        val url = "https://raw.githubusercontent.com/mariotaku/wtb/master/twidere/bots.xml"
        val arguments = UrlFiltersSubscriptionProvider.Arguments().apply {
            this.url = url
        }
        val provider = UrlFiltersSubscriptionProvider(context, arguments)
        provider.fetchFilters()
        provider.sources
    }
}