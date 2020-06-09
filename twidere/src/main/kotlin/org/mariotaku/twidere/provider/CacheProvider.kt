package org.mariotaku.twidere.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8
import org.mariotaku.mediaviewer.library.FileCache
import org.mariotaku.twidere.TwidereConstants.AUTHORITY_TWIDERE_CACHE
import org.mariotaku.twidere.TwidereConstants.QUERY_PARAM_TYPE
import org.mariotaku.twidere.annotation.CacheFileType
import org.mariotaku.twidere.model.CacheMetadata
import org.mariotaku.twidere.task.SaveFileTask
import org.mariotaku.twidere.util.JsonSerializer
import org.mariotaku.twidere.util.dagger.GeneralComponent
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

/**
 * Created by mariotaku on 16/1/1.
 */
class CacheProvider : ContentProvider() {
    @Inject
    internal lateinit var fileCache: FileCache

    override fun onCreate(): Boolean {
        GeneralComponent.get(context!!).inject(this)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
            selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        val metadata = getMetadata(uri)
        if (metadata != null) {
            return metadata.contentType
        }
        when (uri.getQueryParameter(QUERY_PARAM_TYPE)) {
            CacheFileType.IMAGE -> {
                val file = fileCache.get(getCacheKey(uri)) ?: return null
                return BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(file.absolutePath, this)
                }.outMimeType
            }
            CacheFileType.VIDEO -> {
                return "video/mp4"
            }
            CacheFileType.JSON -> {
                return "application/json"
            }
        }
        return null
    }

    fun getMetadata(uri: Uri): CacheMetadata? {
        val bytes = fileCache.getExtra(getCacheKey(uri)) ?: return null
        return try {
            ByteArrayInputStream(bytes).use {
                return@use JsonSerializer.parse(it, CacheMetadata::class.java)
            }
        } catch (e: IOException) {
            null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        try {
            val file = fileCache.get(getCacheKey(uri)) ?: throw FileNotFoundException()
            val modeBits = modeToMode(mode)
            if (modeBits != ParcelFileDescriptor.MODE_READ_ONLY)
                throw IllegalArgumentException("Cache can't be opened for write")
            return ParcelFileDescriptor.open(file, modeBits)
        } catch (e: IOException) {
            throw FileNotFoundException()
        }

    }

    class ContentUriFileInfo(
            private val context: Context,
            private val uri: Uri,
            @CacheFileType override val cacheFileType: String?
    ) : SaveFileTask.FileInfo, CacheFileTypeSupport {
        override val fileName: String by lazy {
            var cacheKey = getCacheKey(uri)
            val indexOfSsp = cacheKey.indexOf("://")
            if (indexOfSsp != -1) {
                cacheKey = cacheKey.substring(indexOfSsp + 3)
            }
            return@lazy cacheKey.replace("[^\\w\\d_]".toRegex(), specialCharacter.toString())
        }

        override val mimeType: String? by lazy {
            if (cacheFileType == null || uri.getQueryParameter(QUERY_PARAM_TYPE) != null) {
                return@lazy context.contentResolver.getType(uri)
            }
            val builder = uri.buildUpon()
            builder.appendQueryParameter(QUERY_PARAM_TYPE, cacheFileType)
            return@lazy context.contentResolver.getType(builder.build())
        }

        override val specialCharacter: Char
            get() = '_'

        override fun inputStream(): InputStream {
            return context.contentResolver.openInputStream(uri)!!
        }

        override fun close() {
            // No-op
        }
    }

    interface CacheFileTypeSupport {
        val cacheFileType: String?
    }


    companion object {

        fun getCacheUri(key: String, @CacheFileType type: String?): Uri {
            val builder = Uri.Builder()
            builder.scheme(ContentResolver.SCHEME_CONTENT)
            builder.authority(AUTHORITY_TWIDERE_CACHE)
            builder.appendPath(key.encodeUtf8().base64Url())
            if (type != null) {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, type)
            }
            return builder.build()
        }

        fun getCacheKey(uri: Uri): String {
            if (ContentResolver.SCHEME_CONTENT != uri.scheme)
                throw IllegalArgumentException(uri.toString())
            if (AUTHORITY_TWIDERE_CACHE != uri.authority)
                throw IllegalArgumentException(uri.toString())
            return uri.lastPathSegment?.decodeBase64()!!.utf8()
        }


        /**
         * Copied from ContentResolver.java
         */
        private fun modeToMode(mode: String): Int {
            return if ("r" == mode) {
                ParcelFileDescriptor.MODE_READ_ONLY
            } else if ("w" == mode || "wt" == mode) {
                ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else if ("wa" == mode) {
                ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_APPEND
            } else if ("rw" == mode) {
                ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            } else if ("rwt" == mode) {
                ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else {
                throw IllegalArgumentException("Invalid mode: $mode")
            }
        }
    }
}
