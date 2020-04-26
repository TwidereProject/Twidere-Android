package org.mariotaku.twidere.util

import android.app.Application
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.FlakyTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.text.TextUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants
import org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME
import org.mariotaku.twidere.task.twitter.UpdateStatusTask

/**
 * Created by mariotaku on 2016/12/7.
 */


@RunWith(AndroidJUnit4::class)
class StatusShortenerInterfaceTest {
    @Test
    @FlakyTest
    fun testConnection() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val application = context.applicationContext as Application
        val preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val shortenerComponent = preferences.getString(TwidereConstants.KEY_STATUS_SHORTENER, null) ?: return
        val instance = StatusShortenerInterface.getInstance(application, shortenerComponent) ?: return
        instance.checkService { metaData ->
            if (metaData == null) throw UpdateStatusTask.ExtensionVersionMismatchException()
            val extensionVersion = metaData.getString(TwidereConstants.METADATA_KEY_EXTENSION_VERSION_STATUS_SHORTENER)
            if (!TextUtils.equals(extensionVersion, context.getString(R.string.status_shortener_service_interface_version))) {
                throw UpdateStatusTask.ExtensionVersionMismatchException()
            }
        }
        Assert.assertTrue(instance.waitForService())
        instance.unbindService()
    }
}
