package org.mariotaku.twidere.service

import android.app.Service
import android.content.Intent
import android.content.SyncResult
import android.os.Bundle
import android.os.IBinder
import org.mariotaku.twidere.IDataSyncService
import org.mariotaku.twidere.activity.DropboxAuthStarterActivity
import org.mariotaku.twidere.model.SyncAuthInfo
import java.lang.ref.WeakReference

/**
 * Created by mariotaku on 2016/12/7.
 */

class DropboxDataSyncService : Service() {
    private val serviceInterface: ServiceInterface

    init {
        serviceInterface = ServiceInterface(WeakReference(this))
    }

    override fun onBind(intent: Intent?): IBinder {
        return serviceInterface.asBinder()
    }

    private fun getAuthInfo(): SyncAuthInfo? {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getAuthRequestIntent(info: SyncAuthInfo?): Intent {
        return Intent(this, DropboxAuthStarterActivity::class.java)
    }

    private fun onPerformSync(info: SyncAuthInfo, extras: Bundle?, syncResult: SyncResult) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    internal class ServiceInterface(val service: WeakReference<DropboxDataSyncService>) : IDataSyncService.Stub() {

        override fun getAuthInfo(): SyncAuthInfo? {
            return service.get().getAuthInfo()
        }

        override fun getAuthRequestIntent(info: SyncAuthInfo?): Intent {
            return service.get().getAuthRequestIntent(info)
        }

        override fun onPerformSync(info: SyncAuthInfo, extras: Bundle?, syncResult: SyncResult) {
            service.get().onPerformSync(info, extras, syncResult)
        }

    }

}
