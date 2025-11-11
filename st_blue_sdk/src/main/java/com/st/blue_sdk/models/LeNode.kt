package com.st.blue_sdk.models

import android.bluetooth.BluetoothDevice
import com.st.blue_sdk.bt.advertise.LeBleAdvertiseInfo

data class LeNode(
    val device: BluetoothDevice,
    val advertiseData: LeBleAdvertiseInfo,
)