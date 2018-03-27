package org.mariotaku.twidere.service

import android.app.IntentService
import com.twitter.Extractor
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.util.NotificationManagerWrapper
import javax.inject.Inject

abstract class BaseIntentService(tag: String) : IntentService(tag) {

    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var extractor: Extractor

    override fun onCreate() {
        super.onCreate()
        GeneralComponent.get(this).inject(this)
    }
}