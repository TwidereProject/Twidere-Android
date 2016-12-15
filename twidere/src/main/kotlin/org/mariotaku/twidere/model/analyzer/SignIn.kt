package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/15.
 */
data class SignIn(
        val success: Boolean,
        @AccountType val type: String,
        override val account: String? = null,
        val officialKey: Boolean = false,
        val authType: String? = null,
        val errorReason: String? = null
) : Analyzer.Event