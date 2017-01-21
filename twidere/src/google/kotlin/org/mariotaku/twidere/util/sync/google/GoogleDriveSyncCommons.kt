package org.mariotaku.twidere.util.sync.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.InputStream

/**
 * Created by mariotaku on 1/21/17.
 */


internal const val folderMimeType = "application/vnd.google-apps.folder"
internal const val xmlMimeType = "application/xml"


internal fun Drive.Files.getOrNull(name: String, mimeType: String?, parent: String? = "root",
                                   trashed: Boolean = false): File? {
    val find = list()
    var query = "name = '$name'"
    if (parent != null) {
        query += " and '$parent' in parents"
    }
    if (mimeType != null) {
        query += " and mimeType = '$mimeType'"
    }
    query += " and trashed = $trashed"
    find.q = query
    try {
        return find.execute().files.firstOrNull()
    } catch (e: GoogleJsonResponseException) {
        if (e.statusCode == 404) {
            return null
        } else {
            throw e
        }
    }
}

internal fun Drive.Files.getOrCreate(name: String, mimeType: String, parent: String = "root",
                                     trashed: Boolean = false): File {
    return getOrNull(name, mimeType, parent, trashed) ?: run {
        val fileMetadata = File()
        fileMetadata.name = name
        fileMetadata.mimeType = mimeType
        fileMetadata.parents = listOf(parent)
        return@run create(fileMetadata).execute()
    }
}

internal fun Drive.Files.updateOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    return run {
        val find = list()
        find.q = "name = '$name' and '$parent' in parents and mimeType = '$mimeType' and trashed = $trashed"
        try {
            val file = find.execute().files.firstOrNull() ?: return@run null
            fileConfig?.invoke(file)
            return@run update(file.id, file, InputStreamContent(mimeType, stream)).execute()
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 404) {
                return@run null
            } else {
                throw e
            }
        }
    } ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        fileConfig?.invoke(file)
        return@run create(file, InputStreamContent(mimeType, stream)).execute()
    }
}