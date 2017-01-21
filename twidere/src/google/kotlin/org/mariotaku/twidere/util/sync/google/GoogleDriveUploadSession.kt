package org.mariotaku.twidere.util.sync.google

import com.dropbox.core.v2.files.UploadUploader
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

abstract internal class GoogleDriveUploadSession<in Data>(
        val name: String,
        val parentId: String,
        val mimeType: String,
        val files: Drive.Files
) : Closeable {
    private var uploader: UploadUploader? = null

    var localModifiedTime: Long = 0

    override fun close() {
        uploader?.close()
    }

    @Throws(IOException::class)
    abstract fun Data.toInputStream(): InputStream

    fun uploadData(data: Data): Boolean {
        files.updateOrCreate(name, mimeType, parentId, stream = data.toInputStream(), fileConfig = {
            it.modifiedTime = DateTime(localModifiedTime)
        })
        return true
    }

}