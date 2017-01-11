package org.mariotaku.twidere.util.sync.dropbox

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.UploadUploader
import java.io.Closeable
import java.io.IOException

abstract internal class DropboxUploadSession<in Data>(val fileName: String, val client: DbxClientV2) : Closeable {
    private var uploader: UploadUploader? = null

    var localModifiedTime: Long = 0

    override fun close() {
        uploader?.close()
    }

    @Throws(IOException::class)
    abstract fun performUpload(uploader: UploadUploader, data: Data)

    fun uploadData(data: Data): Boolean {
        uploader = client.newUploader(fileName, localModifiedTime).apply {
            performUpload(this, data)
            this.finish()
        }
        return true
    }

}