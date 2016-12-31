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
            Log.d(LOGTAG, "Downloading remote filters")
        }
        val remoteModified = remoteFilters.loadFromRemote(snapshotFile.lastModified())
        if (BuildConfig.DEBUG && !remoteModified) {
            Log.d(LOGTAG, "Remote filter unchanged, skipped downloading")
        }
        val filters: FiltersData = FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }

        var localModified = false

        val deletedFilters: FiltersData? = try {
            FileReader(snapshotFile).use {
                val result = FiltersData()
                val parser = Xml.newPullParser()
                parser.setInput(it)
                result.parse(parser)
                localModified = localModified or (result != filters)
                result.removeAll(filters)
                return@use result
            }
        } catch (e: FileNotFoundException) {
            localModified = true
            null
        }

        if (remoteModified) {
            localModified = localModified or filters.addAll(remoteFilters, true)
        }

        if (deletedFilters != null) {
            localModified = localModified or filters.removeAll(deletedFilters)
        }

        filters.write(context.contentResolver)

        val localModifiedTime = System.currentTimeMillis()

        if (localModified) {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, "Uploading filters")
            }
            filters.saveToRemote(localModifiedTime)
        } else if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Local not modified, skip upload")
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
            Log.d(LOGTAG, "Filters sync complete")
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