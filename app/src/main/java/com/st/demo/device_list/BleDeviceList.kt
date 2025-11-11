/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
@file:OptIn(ExperimentalPermissionsApi::class)

package com.st.demo.device_list

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.st.demo.R
import kotlinx.coroutines.delay

fun ByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }

@Composable
fun LeDevice(
    modifier: Modifier = Modifier,
    timestamp: Long,
    deviceName: String,
    deviceAddress: String,
    protocolDeviceId: Int,
    protocolFwId: Int,
    protocolId: Int,
    payloadData: String
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        shadowElevation = 10.dp
    ) {

        var isAnimated by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = timestamp) {
            if (timestamp!=0L) {
                isAnimated = true
                delay(100)
                isAnimated = false
            }
        }

        val animatedColor by animateColorAsState(
            if (isAnimated) {
                Color.Cyan
            } else {
                Color.Black
            },
            label = "color"
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Name = $deviceName"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "Address = $deviceAddress"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "DeviceID = 0x${
                    Integer.toHexString(protocolDeviceId)
                        .padStart(2, '0')
                }"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "firmwareId = 0x${
                    Integer.toHexString(protocolFwId)
                        .padStart(2, '0')
                }"
            )
            Text(
                modifier = Modifier.padding(4.dp),
                text = "protocolId = 0x${
                    Integer.toHexString(protocolId)
                        .padStart(4, '0')
                }"
            )
            Row {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "payloadData = "
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    color = animatedColor,
                    text = "0x${
                        payloadData
                    }"
                )
            }
        }
    }
}

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterialApi::class
)
@SuppressLint("MissingPermission")
@Composable
fun BleDeviceList(
    viewModel: BleDeviceListViewModel, navController: NavHostController
) {

    var doNotShowRationale by rememberSaveable {
        mutableStateOf(false)
    }

    val locationPermissionState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        else listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (locationPermissionState.allPermissionsGranted) {
        // remember calculates the value passed to it only during the first composition. It then
        // returns the same value for every subsequent composition. More details are available in the
        // comments below.
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .scrollable(
                    state = scrollState, orientation = Orientation.Vertical
                )
        ) {
            val devices = viewModel.scanBleDevices.collectAsState()
            val devicesLe = viewModel.scanBleLeDevices.collectAsState()
            val isBleScanning by viewModel.isLEScanning.collectAsState()
            val isRefreshing by viewModel.isLoading.collectAsState()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ElevatedButton(onClick = { viewModel.startScan(false) }) {
                    Text(
                        "Scan Devices\nBlueST-SDK",
                        textAlign = TextAlign.Center
                    )
                }
                ElevatedButton(onClick = { viewModel.startScan(true) }) {
                    Text(
                        "Scan Devices\nBlueST-SDK-LE",
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if(isBleScanning) {
                Text(text = stringResource(R.string.st_le_deviceList_title))
            } else {
                Text(text = stringResource(R.string.st_deviceList_title))
            }

            Spacer(modifier = Modifier.height(4.dp))


            val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
                viewModel.scanSelectedDevicesType()
            })

            if (devices.value.isEmpty() && devicesLe.value.second.isEmpty() && isRefreshing.not()) {
                Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {
                    Text(
                        text = "Press one button\nfor searching\ncompatible devices",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(modifier = Modifier.pullRefresh(state = pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(
                        items = devices.value,
                        key = { _, item -> item.device.address }) { index, item ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            shadowElevation = 10.dp,
                            onClick = {
                                navController.navigate("detail/${item.device.address}")
                            }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "Name = ${item.device.name}"
                                )
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "Address = ${item.device.address}"
                                )
                            }
                        }

                        if (devices.value.lastIndex != index) {
                            HorizontalDivider()
                        }
                    }

                    itemsIndexed(
                        items = devicesLe.value.second,
                        key = { _, item -> item.device.address }) { index, item ->
                        LeDevice(
                            deviceName = item.device.name,
                            deviceAddress = item.device.address,
                            protocolDeviceId = item.advertiseData.getDeviceId(),
                            protocolFwId = item.advertiseData.getFwId(),
                            protocolId = item.advertiseData.getProtocolId(),
                            payloadData = item.advertiseData.getPayloadData().toHexString(),
                            timestamp = devicesLe.value.first
                        )

                        if (devicesLe.value.second.lastIndex != index) {
                            HorizontalDivider()
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    scale = true
                )
            }
        }

//        LaunchedEffect(key1 = Unit) {
//            viewModel.startScan(false)
//        }
    } else {
        if (doNotShowRationale) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text("Feature not available")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text("The Location and Record Audio is important for this app. Please grant the permission.")

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.weight(0.5f),
                        onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text("Ok!")
                    }

                    Spacer(Modifier.width(4.dp))

                    Button(
                        modifier = Modifier.weight(0.5f),
                        onClick = { doNotShowRationale = true }) {
                        Text("Nope")
                    }
                }
            }
        }
    }
}