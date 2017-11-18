package org.mariotaku.twidere.model.sync

import android.content.Intent

data class SyncProviderInfo(val type: String, val name: String, val authIntent: Intent)
