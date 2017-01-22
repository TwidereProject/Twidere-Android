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
internal const val requiredRequestFields = "id, name, mimeType, modifiedTime"

internal fun Drive.getFileOrNull(
        name: String,
        mimeType: String?,
        parent: String? = "root",
        trashed: Boolean = false,
        conflictResolver: ((Drive, List<File>) -> File)? = null
): File? {
    val result = findFilesOrNull(name, mimeType, parent, trashed) ?: return null
    if (result.size > 1 && conflictResolver != null) {
        return conflictResolver(this, result)
    }
    return result.firstOrNull()
}

internal fun Drive.findFilesOrNull(
        name: String,
        mimeType: String?,
        parent: String? = "root",
        trashed: Boolean = false
): List<File>? {
    val find = files().list()
    var query = "name = '$name'"
    if (parent != null) {
        query += " and '$parent' in parents"
    }
    if (mimeType != null) {
        query += " and mimeType = '$mimeType'"
    }
    query += " and trashed = $trashed"
    find.q = query
    find.fields = "files($requiredRequestFields)"
    try {
        return find.execute().files
    } catch (e: GoogleJsonResponseException) {
        if (e.statusCode == 404) {
            return null
        } else {
            throw e
        }
    }
}

internal fun Drive.getFileOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        conflictResolver: ((Drive, List<File>) -> File)? = null
): File {
    val result = findFilesOrCreate(name, mimeType, parent, trashed)
    if (result.size > 1 && conflictResolver != null) {
        return conflictResolver(this, result)
    }
    return result.first()
}

internal fun Drive.findFilesOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false
): List<File> {
    return findFilesOrNull(name, mimeType, parent, trashed) ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        val create = files().create(file)
        return@run listOf(create.execute())
    }
}

internal fun Drive.updateOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    val files = files()
    return run {
        val find = files.list()
        find.q = "name = '$name' and '$parent' in parents and mimeType = '$mimeType' and trashed = $trashed"
        val fileId = try {
            find.execute().files.firstOrNull()?.id ?: return@run null
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 404) {
                return@run null
            } else {
                throw e
            }
        }
        return@run files.performUpdate(fileId, name, mimeType, stream, fileConfig)
    } ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        fileConfig?.invoke(file)
        val create = files.create(file, InputStreamContent(mimeType, stream))
        return@run create.execute()
    }
}

internal fun Drive.Files.performUpdate(
        fileId: String,
        name: String,
        mimeType: String,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    val file = File()
    file.name = name
    file.mimeType = mimeType
    fileConfig?.invoke(file)
    val update = update(fileId, file, InputStreamContent(mimeType, stream))
    update.fields = requiredRequestFields
    return update.execute()
}

internal fun resolveFilesConflict(client: Drive, list: List<File>): File {
    // Use newest file
    val newest = list.maxBy { it.modifiedTime.value }!!

    // Delete all others
    val batch = client.batch()
    val callback = SimpleJsonBatchCallback<Void>()
    val files = client.files()
    list.filterNot { it == newest }.forEach { files.delete(it.id).queue(batch, callback) }
    batch.execute()
    return newest
}

internal fun resolveFoldersConflict(client: Drive, list: List<File>): File {
    return list.first()
}