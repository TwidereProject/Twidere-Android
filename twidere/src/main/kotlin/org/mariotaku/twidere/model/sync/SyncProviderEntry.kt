package org.mariotaku.twidere.model.sync

import android.content.Intent

/**
 * Created by mariotaku on 2017/1/2.
 */

data class SyncProviderEntry(val type: String, val name: String, val authIntent: Intent)
