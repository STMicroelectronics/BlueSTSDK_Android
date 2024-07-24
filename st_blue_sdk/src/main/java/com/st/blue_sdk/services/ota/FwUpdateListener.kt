/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.services.ota

enum class FwUploadError {

    /**
     * error fired when the crc computed in the node isn't equal to the one computed on the
     * mobile.
     * this can happen when there are some error during the transmission
     */
    ERROR_CORRUPTED_FILE,

    /**
     * error fired when is not possible upload all the file
     */
    ERROR_TRANSMISSION,

    /**
     * error fired when is not possible open the file to upload
     */
    ERROR_INVALID_FW_FILE,

    /**
     * the node firmware has a wrong version
     */
    ERROR_WRONG_SDK_VERSION,


    ERROR_WRONG_SDK_VERSION_OR_ERROR_TRANSMISSION,

    /**
     * error when the file is not downloaded
     */
    ERROR_DOWNLOADING_FILE,

    ERROR_UNKNOWN
}

interface FwUpdateListener {

    fun onUpdate(progress: Float)

    fun onComplete()

    fun onError(error: FwUploadError)
}