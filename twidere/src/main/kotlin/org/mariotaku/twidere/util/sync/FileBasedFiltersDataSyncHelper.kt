package org.mariotaku.twidere.util.sync

import android.content.Context
import android.util.Log
import android.util.Xml
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.FiltersData
import java.io.*

abstract class FileBasedFiltersDataSyncHelper(val context: Context) {
    fun performSync(): Boolean {
        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: return false
        val snapshotFile = File(syncDataDir, "filters.xml")

        val remoteFilters = FiltersData()
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Downloading remote filters")
        }
        val remoteModified = remoteFilters.loadFromRemote(snapshotFile.lastModified())
        if (BuildConfig.DEBUG && !remoteModified) {
            Log.d(LOGTAG_SYNC, "Remote filter unchanged, skip download")
        }
        val filters: FiltersData = FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }

        var localModified = false

        val remoteAddedFilters = FiltersData()
        val remoteDeletedFilters = FiltersData()
        try {
            val snapshot = FileReader(snapshotFile).use {
                val snapshot = FiltersData()
                val parser = Xml.newPullParser()
                parser.setInput(it)
                snapshot.parse(parser)
                return@use snapshot
            }
            if (remoteModified) {
                remoteAddedFilters.addAll(remoteFilters)
                remoteAddedFilters.removeAll(snapshot)

                remoteDeletedFilters.addAll(snapshot)
                remoteDeletedFilters.removeAll(remoteFilters)
            }

            localModified = localModified or (snapshot != filters)
        } catch (e: FileNotFoundException) {
            remoteAddedFilters.addAll(remoteFilters)
            remoteAddedFilters.removeAll(filters)

            localModified = true
        }

        if (remoteModified) {
            filters.addAll(remoteAddedFilters, true)
            filters.removeAll(remoteDeletedFilters)

            localModified = !remoteAddedFilters.isEmpty() || !remoteDeletedFilters.isEmpty()
        }

        filters.write(context.contentResolver)

        val localModifiedTime = System.currentTimeMillis()

        if (localModified) {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG_SYNC, "Uploading filters")
            }
            filters.saveToRemote(localModifiedTime)
        } else if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Local not modified, skip upload")
        }
        try {
            FileWriter(snapshotFile).use {
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(it)
                filters.serialize(serializer)
            }
            snapshotFile.setLastModified(localModifiedTime)
        } catch (e: FileNotFoundException) {
            // Ignore
        }

        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Filters sync complete")
        }
        return true
    }

    /**
     * Return false if remote not changed
     */
    @Throws(IOException::class)
    protected abstract fun FiltersData.loadFromRemote(snapshotModifiedMillis: Long): Boolean

    @Throws(IOException::class)
    protected abstract fun FiltersData.saveToRemote(localModifiedTime: Long): Boolean

}