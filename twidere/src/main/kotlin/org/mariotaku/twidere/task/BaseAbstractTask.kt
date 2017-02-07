package org.mariotaku.twidere.task

import android.content.Context
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.KPreferences
import org.mariotaku.twidere.util.AsyncTwitterWrapper
import org.mariotaku.twidere.util.MediaLoaderWrapper
import org.mariotaku.twidere.util.SharedPreferencesWrapper
import org.mariotaku.twidere.util.UserColorNameManager
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper
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
    lateinit var mediaLoader: MediaLoaderWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var kPreferences: KPreferences
    @Inject
    lateinit var manager: UserColorNameManager

    init {
        @Suppress("UNCHECKED_CAST", "LeakingThis")
        GeneralComponentHelper.build(context).inject(this as BaseAbstractTask<Any, Any, Any>)
    }


}
