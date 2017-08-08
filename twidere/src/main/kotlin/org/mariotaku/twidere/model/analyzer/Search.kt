package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/15.
 */

data class Search(
        val query: String,
        @AccountType override val accountType: String?,
        override val accountHost: String? = null
) : Analyzer.Event
