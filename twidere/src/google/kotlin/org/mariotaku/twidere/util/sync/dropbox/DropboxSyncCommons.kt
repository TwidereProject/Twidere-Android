package org.mariotaku.twidere.util.sync.dropbox

import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import com.dropbox.core.v2.files.WriteMode
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

/**
 * Created by mariotaku on 2017/1/10.
 */

@Throws(IOException::class)
internal fun DbxClientV2.newUploader(path: String, clientModified: Long): UploadUploader {
    try {
        return files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true)
                .withClientModified(Date(clientModified)).start()
    } catch (e: DbxException) {
        throw IOException(e)
    }
}

@Throws(IOException::class)
internal fun DbxClientV2.newDownloader(path: String): DbxDownloader<FileMetadata> {
    try {
        return files().downloadBuilder(path).start()
    } catch (e: DownloadErrorException) {
        if (e.errorValue?.pathValue?.isNotFound ?: false) {
            throw FileNotFoundException(path)
        }
        throw IOException(e)
    } catch (e: DbxException) {
        throw IOException(e)
    }
}