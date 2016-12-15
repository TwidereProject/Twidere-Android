package org.mariotaku.twidere.model.analyzer

import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.model.account.cred.Credentials
import org.mariotaku.twidere.util.Analyzer

/**
 * Created by mariotaku on 2016/12/15.
 */
data class SignIn(
        val success: Boolean,
        val officialKey: Boolean = false,
        @Credentials.Type val credentialsType: String? = null,
        val errorReason: String? = null,
        @AccountType override val accountType: String,
        override val accountHost: String? = null
) : Analyzer.Event