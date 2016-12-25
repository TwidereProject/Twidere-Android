package org.mariotaku.twidere.util.premium

/**
 * Created by mariotaku on 2016/12/25.
 */

class DummyExtraFeaturesChecker() : ExtraFeaturesChecker() {

    override fun isSupported(): Boolean = false

    override fun isEnabled(): Boolean = false

}
