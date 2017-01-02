package org.mariotaku.twidere.util.sync

import android.content.Context
import android.util.Xml
import org.mariotaku.ktextension.nullableContentEquals
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.FiltersData
import java.io.Closeable
import java.io.File
import java.io.IOException

abstract class FileBasedFiltersDataSyncHelper<DownloadSession : Closeable, UploadSession : Closeable>(
        val context: Context
) : SingleFileBasedDataSyncHelper<FiltersData, File, DownloadSession, UploadSession>() {

    override fun File.loadSnapshot(): FiltersData {
        return reader().use {
            val snapshot = FiltersData()
            val parser = Xml.newPullParser()
            parser.setInput(it)
            snapshot.parse(parser)
            return@use snapshot
        }
    }

    override fun File.saveSnapshot(data: FiltersData) {
        writer().use {
            val serializer = Xml.newSerializer()
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.setOutput(it)
            data.serialize(serializer)
        }
    }

    override var File.snapshotLastModified: Long
        get() = this.lastModified()
        set(value) {
            this.setLastModified(value)
        }

    override fun loadFromLocal(): FiltersData {
        return FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }
    }

    override fun FiltersData.saveToLocal() {
        this.write(context.contentResolver)
    }

    override fun newData(): FiltersData {
        return FiltersData()
    }

    override fun FiltersData.minus(data: FiltersData): FiltersData {
        val diff = FiltersData()
        diff.addAll(this, true)
        diff.removeAllData(data)
        return diff
    }

    override fun FiltersData.addAllData(data: FiltersData): Boolean {
        return this.addAll(data, ignoreDuplicates = true)
    }

    override fun FiltersData.removeAllData(data: FiltersData): Boolean {
        return this.removeAll(data)
    }

    override fun FiltersData.isDataEmpty(): Boolean {
        return this.isEmpty()
    }

    override fun newSnapshotStore(): File {
        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: throw IOException()
        return File(syncDataDir, "filters.xml")
    }

    override fun FiltersData.dataContentEquals(localData: FiltersData): Boolean {
        return this.users.nullableContentEquals(localData.users)
                && this.keywords.nullableContentEquals(localData.keywords)
                && this.sources.nullableContentEquals(localData.sources)
                && this.links.nullableContentEquals(localData.links)
    }

    override val whatData: String = "filters"
}