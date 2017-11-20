package org.mariotaku.twidere.task

import android.content.Context
import android.content.SharedPreferences
import com.squareup.otto.Bus
import com.twitter.Extractor
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.restfu.http.RestHttpClient
import org.mariotaku.twidere.dagger.component.GeneralComponent
import org.mariotaku.twidere.extension.get
import org.mariotaku.twidere.model.DefaultFeatures
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.ReadStateManager
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.cache.JsonCache
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.schedule.StatusScheduleProvider
import org.mariotaku.twidere.util.sync.DataSyncProvider
import org.mariotaku.twidere.util.sync.SyncPreferences
import javax.inject.Inject

abstract class BaseAbstractTask<Params, Result, Callback>(val context: Context) : AbstractTask<Params, Result, Callback>() {

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var microBlogWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var mediaPreloader: MediaPreloader
    @Inject
    lateinit var preferences: SharedPreferences
    @Inject
    lateinit var manager: UserColorNameManager
    @Inject
    lateinit var errorInfoStore: ErrorInfoStore
    @Inject
    lateinit var readStateManager: ReadStateManager
    @Inject
    lateinit var userColorNameManager: UserColorNameManager
    @Inject
    lateinit var extraFeaturesService: ExtraFeaturesService
    @Inject
    lateinit var restHttpClient: RestHttpClient
    @Inject
    lateinit var defaultFeatures: DefaultFeatures
    @Inject
    lateinit var scheduleProvider: StatusScheduleProvider
    @Inject
    lateinit var extractor: Extractor
    @Inject
    lateinit var syncPreferences: SyncPreferences
    @Inject
    lateinit var dataSyncProvider: DataSyncProvider
    @Inject
    lateinit var jsonCache: JsonCache

    init {
        injectMembers()
    }

    private fun injectMembers() {
        @Suppress("UNCHECKED_CAST")
        GeneralComponent.get(context).inject(this as BaseAbstractTask<Any, Any, Any>)
    }

}
