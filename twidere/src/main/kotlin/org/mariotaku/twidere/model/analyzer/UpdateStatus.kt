package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/28.
 */

data class UpdateStatus(
        @AccountType override val accountType: String? = null
) : Analyzer.Event {

}
