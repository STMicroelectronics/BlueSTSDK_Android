/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

class UnwrapTimestamp {

    companion object {
        private const val NEAR_TO_END_TH = (1 shl 16) - 100
    }

    private var reset = 0L
    private var lastTimestamp = 0L

    fun next() = unwrap(lastTimestamp + 1)

    fun unwrap(ts: Long): Long {
        if (lastTimestamp > NEAR_TO_END_TH && lastTimestamp > ts)
            reset++
        lastTimestamp = ts

        return reset * (1 shl 16) + ts
    }
}
