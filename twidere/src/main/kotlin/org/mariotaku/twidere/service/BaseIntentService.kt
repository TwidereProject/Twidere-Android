package org.mariotaku.twidere.service

import android.app.IntentService
import com.twitter.Extractor
import com.twitter.Validator
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import javax.inject.Inject

abstract class BaseIntentService(tag: String) : IntentService(tag) {

    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var notificationManager: NotificationManagerWrapper
    @Inject
    lateinit var validator: Validator
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    override fun onCreate() {
        super.onCreate()
        GeneralComponentHelper.build(this).inject(this)
    }
}