/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_sdk.bt

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BleBondingBroadcastReceiver(
    val onBondStateChanged: (bondState: Int, deviceAddress: String?) -> Unit
) : BroadcastReceiver() {

    companion object {
        val TAG = BleBondingBroadcastReceiver::class.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {

        with(intent) {
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {

                val device = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val previousBondState = getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)
                val bondState = getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)

                val bondTransition =
                    "${previousBondState.toBondStateDescription()} to " + bondState.toBondStateDescription()

                Log.d(
                    TAG,
                    "${device?.address} bond state changed | $bondTransition"
                )

                onBondStateChanged(bondState, device?.address)
            }
        }
    }

    private fun Int.toBondStateDescription() = when (this) {
        BluetoothDevice.BOND_BONDED -> "BONDED"
        BluetoothDevice.BOND_BONDING -> "BONDING"
        BluetoothDevice.BOND_NONE -> "NOT BONDED"
        else -> "ERROR: $this"
    }
}