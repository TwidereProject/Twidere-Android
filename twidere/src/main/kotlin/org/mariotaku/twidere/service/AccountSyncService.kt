package org.mariotaku.twidere.service

import android.accounts.Account
import android.content.*
import android.os.Bundle
import android.os.IBinder


/**
 * Created by mariotaku on 2016/12/3.
 */
class AccountSyncService : BaseService() {

    override fun onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized(sSyncAdapterLock) {
            syncAdapter = SyncAdapter(applicationContext, true)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return syncAdapter.syncAdapterBinder
    }

    internal class SyncAdapter(context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {
        override fun onPerformSync(account: Account, extras: Bundle, authority: String,
                provider: ContentProviderClient, syncResult: SyncResult) {
        }
    }

    companion object {

        private lateinit var syncAdapter: SyncAdapter
        private val sSyncAdapterLock = Any()
    }
}