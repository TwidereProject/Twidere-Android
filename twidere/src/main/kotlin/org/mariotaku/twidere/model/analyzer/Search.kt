package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/15.
 */

data class Search(
        val query: String,
        override val account: String?
) : Analyzer.Event
