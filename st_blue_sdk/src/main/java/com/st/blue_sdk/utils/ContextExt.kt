/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.utils

import android.Manifest.permission.BLUETOOTH
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

fun Context.hasBluetoothPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, BLUETOOTH) == PackageManager.PERMISSION_GRANTED
