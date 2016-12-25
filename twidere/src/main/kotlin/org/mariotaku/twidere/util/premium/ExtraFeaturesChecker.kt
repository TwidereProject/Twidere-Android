package org.mariotaku.twidere.util.premium

import android.content.Context
import android.support.annotation.CallSuper
import java.util.*

/**
 * Created by mariotaku on 2016/12/25.
 */

abstract class ExtraFeaturesChecker {
    protected lateinit var context: Context

    @CallSuper
    open fun init(context: Context) {
        this.context = context
    }

    open fun release() {
    }

    abstract fun isSupported(): Boolean

    abstract fun isEnabled(): Boolean

    companion object {

        val instance: ExtraFeaturesChecker
            get() = ServiceLoader.load(ExtraFeaturesChecker::class.java).first()

    }
}
