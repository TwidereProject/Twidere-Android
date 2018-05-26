package org.mariotaku.twidere.util.sync

import android.util.Log
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.util.DebugLog
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.IOException

abstract class SingleFileBasedDataSyncAction<Data, SnapshotStore, DownloadSession : Closeable, UploadSession : Closeable> : ISyncAction {
    private val TAG_ITEMS = "items"
    private val TAG_ITEM = "item"

    private val ATTR_KEY = "key"

    final override fun execute(): Boolean {
        DebugLog.d(LOGTAG_SYNC, "Begin syncing $whatData")

        if (!setup()) {
            return false
        }

        val snapshotStore = newSnapshotStore()

        var remoteData: Data? = null

        var remoteModified = false
        var shouldCreateRemote = false

        try {
            newLoadFromRemoteSession().use {
                remoteModified = it.getRemoteLastModified() - snapshotStore.snapshotLastModified > 1000
                if (remoteModified) {
                    if (BuildConfig.DEBUG) {
                        Log.d(LOGTAG_SYNC, "Downloading remote $whatData")
                    }
                    remoteData = it.loadFromRemote()
                } else {
                    DebugLog.d(LOGTAG_SYNC, "Remote $whatData unchanged, skip download")
                }
            }
        } catch (e: FileNotFoundException) {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG_SYNC, "Remote $whatData doesn't exist, will upload new one")
            }
            shouldCreateRemote = true
        }

        val localData: Data = loadFromLocal()

        var localModified = false

        var remoteAddedData: Data? = null
        var remoteDeletedData: Data? = null
        try {
            val snapshot = snapshotStore.loadSnapshot()
            if (remoteModified && remoteData != null) {
                remoteAddedData = remoteData!! - snapshot
                remoteDeletedData = snapshot - remoteData!!
            }

            localModified = localModified or !snapshot.dataContentEquals(localData)
        } catch (e: IOException) {
            if (remoteData != null) {
                remoteAddedData = remoteData!! - localData
            }

            localModified = true
        }

        if (remoteModified) {
            if (remoteDeletedData != null) {
                localData.removeAllData(remoteDeletedData)
                removeFromLocal(remoteDeletedData)
                localModified = localModified or !remoteDeletedData.isDataEmpty()
            }
            if (remoteAddedData != null) {
                localData.addAllData(remoteAddedData)
                addToLocal(remoteAddedData)
                localModified = localModified or !remoteAddedData.isDataEmpty()
            }
        }

        val localModifiedTime = System.currentTimeMillis()

        if (shouldCreateRemote || localModified) {
            DebugLog.d(LOGTAG_SYNC, "Uploading $whatData")
            newSaveToRemoteSession().use {
                it.setRemoteLastModified(localModifiedTime)
                it.saveToRemote(localData)
            }
        } else if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Local $whatData not modified, skip upload")
        }
        try {
            snapshotStore.saveSnapshot(localData)
            snapshotStore.snapshotLastModified = localModifiedTime
        } catch (e: FileNotFoundException) {
            // Ignore
        }

        DebugLog.d(LOGTAG_SYNC, "Finished syncing $whatData")
        return true
    }

    protected abstract fun newSnapshotStore(): SnapshotStore

    protected abstract fun newData(): Data

    // Data operations as a collection
    protected abstract operator fun Data.minus(data: Data): Data

    protected abstract fun Data.addAllData(data: Data): Boolean

    protected abstract fun Data.removeAllData(data: Data): Boolean

    protected abstract fun Data.isDataEmpty(): Boolean

    // Snapshot operations
    @Throws(IOException::class)
    protected abstract fun SnapshotStore.loadSnapshot(): Data

    @Throws(IOException::class)
    protected abstract fun SnapshotStore.saveSnapshot(data: Data)

    protected abstract var SnapshotStore.snapshotLastModified: Long

    // Local save/load operations
    protected abstract fun loadFromLocal(): Data

    protected abstract fun addToLocal(data: Data)

    protected abstract fun removeFromLocal(data: Data)

    // Remote operations
    @Throws(FileNotFoundException::class, IOException::class)
    protected abstract fun newLoadFromRemoteSession(): DownloadSession

    @Throws(IOException::class)
    protected abstract fun newSaveToRemoteSession(): UploadSession

    protected abstract fun DownloadSession.getRemoteLastModified(): Long

    protected abstract fun UploadSession.setRemoteLastModified(lastModified: Long)

    @Throws(IOException::class)
    protected abstract fun DownloadSession.loadFromRemote(): Data

    @Throws(IOException::class)
    protected abstract fun UploadSession.saveToRemote(data: Data): Boolean

    protected abstract fun Data.dataContentEquals(localData: Data): Boolean

    protected open fun setup(): Boolean = true

    protected open val whatData: String = "data"

}
