package com.example.infrastructure

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Copies user-picked images from the zero-permission Android Photo Picker into private
 * app-internal storage so diary attachments stay 100% offline and resilient.
 */
object AttachmentStorage {
    private const val DIR_NAME = "diary_attachments"

    suspend fun copyPickedImageToInternal(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val baseDir = File(context.filesDir, DIR_NAME).apply { mkdirs() }
                val destFile = File(baseDir, "att_${UUID.randomUUID()}.jpg")
                require(destFile.canonicalPath.startsWith(baseDir.canonicalPath + File.separator)) {
                    "Invalid attachment path"
                }
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: error("Unable to open image stream")
                destFile.absolutePath
            }
        }

    fun deleteInternalAttachment(context: Context, filePath: String) {
        runCatching {
            val baseDir = File(context.filesDir, DIR_NAME)
            val target = File(filePath)
            if (target.canonicalPath.startsWith(baseDir.canonicalPath + File.separator) && target.exists()) {
                target.delete()
            }
        }
    }
}
