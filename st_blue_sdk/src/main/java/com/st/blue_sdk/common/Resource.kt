/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.common

/**
 * Envelop response successful (with data) and response failure (with message).
 *
 * @param status the [Status]
 * @param data the data in case of successful resource
 * @param code optional code for error details
 * @param message the error message id in case of resource failure
 */
data class Resource<out T>(val status: Status, val data: T?, val code: Int?, val message: Int?) {

    companion object {

        fun <T> success(data: T? = null): Resource<T> = Resource(Status.SUCCESS, data, null, null)

        fun <T> error(msg: Int, code: Int? = null, data: T? = null): Resource<T> =
            Resource(Status.ERROR, data, code, msg)

        fun <T> loading(data: T? = null): Resource<T> = Resource(Status.LOADING, data, null, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
