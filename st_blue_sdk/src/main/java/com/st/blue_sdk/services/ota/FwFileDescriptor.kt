/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Utility class that read a img file converting the char data into byte.
 * the file is read as block of uint32(8char) and it will invert the byte order
 * it the file as the string "01020304" a sequence of read will return the bytes: 0x04,0x03,0x02,
 * 0x01
 */
class FwFileDescriptor(
    private val resolver: ContentResolver,
    private val fileUri: Uri
) {

    enum class FirmwareFileType {
        BIN, IMG, UNKNOWN
    }

    val type: FirmwareFileType

    val length: Long

    init {
        type = getFileType()
        length = getFileLength()
    }

    private fun getFileType(): FirmwareFileType {
        fileUri.lastPathSegment?.lowercase(Locale.getDefault())?.let { name ->
            return when {
                name.endsWith("bin") -> FirmwareFileType.BIN
                name.endsWith("img") -> FirmwareFileType.IMG
                else -> FirmwareFileType.UNKNOWN
            }
        } ?: return FirmwareFileType.UNKNOWN
    }

    private fun getFileLength(): Long {
        return kotlin.runCatching {
            val stream = resolver.openInputStream(fileUri)!!
            var nBytes = 0
            try {
                while (stream.read() >= 0) {
                    nBytes++;
                }//while
            } catch (e: IOException) {
                nBytes = 0;
            }//try-catch

            return nBytes.toLong()
        }.getOrDefault(0)
    }

    fun openFile(): InputStream? {
        return kotlin.runCatching {
            val stream = resolver.openInputStream(fileUri)
            return if (type == FirmwareFileType.IMG) ImgFileInputStream(stream, length) else stream
        }.getOrNull()
    }

    fun getFileSize(): Long {

        val scheme = fileUri.scheme ?: return 0
        if (scheme == "file") {
            return fileUri.path?.let {
                val f = File(it)
                return f.length()
            } ?: 0
        }

        if (scheme == "content") {
            val cursor = resolver.query(fileUri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index > 0) {
                    val fileSize = cursor.getString(index)
                    cursor.close()
                    return fileSize.toLong(10)
                }
            }
        }
        return 0
    }
}