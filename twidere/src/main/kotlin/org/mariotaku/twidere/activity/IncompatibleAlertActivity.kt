package org.mariotaku.twidere.activity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_device_incompatible.*
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import java.util.*

/**
 * Created by mariotaku on 16/4/4.
 */
class IncompatibleAlertActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_incompatible)

        infoText.append(String.format(Locale.US, "Twidere version %s (%d)\n",
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))
        infoText.append(String.format(Locale.US, "Classpath %s\n", ClassLoader.getSystemClassLoader()))
        infoText.append(String.format(Locale.US, "Brand %s\n", Build.BRAND))
        infoText.append(String.format(Locale.US, "Device %s\n", Build.DEVICE))
        infoText.append(String.format(Locale.US, "Display %s\n", Build.DISPLAY))
        infoText.append(String.format(Locale.US, "Hardware %s\n", Build.HARDWARE))
        infoText.append(String.format(Locale.US, "Manufacturer %s\n", Build.MANUFACTURER))
        infoText.append(String.format(Locale.US, "Model %s\n", Build.MODEL))
        infoText.append(String.format(Locale.US, "Product %s\n", Build.PRODUCT))
    }

}
