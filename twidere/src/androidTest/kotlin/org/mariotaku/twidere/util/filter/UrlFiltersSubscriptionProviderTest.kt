package org.mariotaku.twidere.util.filter

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.filter.UrlFiltersSubscriptionProviderArguments

/**
 * Created by mariotaku on 2017/1/11.
 */
@RunWith(AndroidJUnit4::class)
class UrlFiltersSubscriptionProviderTest {
    @Test
    fun testFetchXml() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (cm.activeNetworkInfo?.isConnected != true) return

        val url = "https://raw.githubusercontent.com/mariotaku/wtb/master/twidere/bots.xml"
        val arguments = UrlFiltersSubscriptionProviderArguments().apply {
            this.url = url
        }
        val provider = UrlFiltersSubscriptionProvider(context, arguments)
        provider.fetchFilters()
        provider.sources
    }
}