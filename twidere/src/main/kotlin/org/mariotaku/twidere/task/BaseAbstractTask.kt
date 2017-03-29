package org.mariotaku.twidere.task

import android.content.Context
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
import org.mariotaku.twidere.util.media.MediaPreloader
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import org.mariotaku.twidere.util.schedule.StatusScheduleProvider
import javax.inject.Inject

/**
 * Created by mariotaku on 2017/2/7.
 */

abstract class BaseAbstractTask<Params, Result, Callback>(val context: Context) : AbstractTask<Params, Result, Callback>() {

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var microBlogWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var mediaPreloader: MediaPreloader
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var kPreferences: KPreferences
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
    lateinit var scheduleProviderFactory: StatusScheduleProvider.Factory

    val scheduleProvider: StatusScheduleProvider?
        get() = scheduleProviderFactory.newInstance(context)

    init {
        injectMembers()
    }

    private fun injectMembers() {
        @Suppress("UNCHECKED_CAST")
        GeneralComponentHelper.build(context).inject(this as BaseAbstractTask<Any, Any, Any>)
    }
}
