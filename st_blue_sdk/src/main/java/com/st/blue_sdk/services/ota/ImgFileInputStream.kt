/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota

import java.io.InputStream
import java.util.*

class ImgFileInputStream(stream: InputStream?, streamByteLength: Long) : InputStream() {

    /**
     * utility class used for read a line from the file
     */
    private var mScanner: Scanner? = null

    /**
     * stack used for keep the line content
     */
    private val mBuffer = ArrayDeque<Int>(4)

    private var mFileSize: Long = 0

    init {
        val nLine = streamByteLength / 10
        mFileSize = (streamByteLength - 2 * nLine) / 2
        mScanner = Scanner(stream)
    }

    /**
     * fill the buffer stack with the line
     * @return true if the read is ok, false if it reach the EOF
     */
    private fun readLine(): Boolean {
        if (!mScanner!!.hasNextLine()) return false
        //else
        val line = mScanner!!.nextLine()
        for (i in 0..3) {
            val value = line.substring(2 * i, 2 * i + 2)
            mBuffer.add(value.toInt(16))
        }
        return true
    }

    override fun read(): Int {
        if (mBuffer.isEmpty()) if (!readLine()) // if the read fail, the file ended
            return -1
        return mBuffer.removeLast()
    }

    override fun markSupported(): Boolean {
        return false
    }

    fun length(): Long {
        return mFileSize
    }
}