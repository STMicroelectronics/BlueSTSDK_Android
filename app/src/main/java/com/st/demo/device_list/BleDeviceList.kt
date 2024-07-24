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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.st.demo.R

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
            modifier = Modifier.fillMaxSize().padding(8.dp).scrollable(
                state = scrollState, orientation = Orientation.Vertical
            )
        ) {
            Text(text = stringResource(R.string.st_deviceList_title))

            Spacer(modifier = Modifier.height(4.dp))

            val devices = viewModel.scanBleDevices.collectAsState(initial = emptyList())

            val isRefreshing by viewModel.isLoading.collectAsState()

            val pullRefreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
                viewModel.startScan()
            })
            Box(modifier = Modifier.pullRefresh(state = pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(items = devices.value) { index, item ->
                        Surface(modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            shadowElevation = 10.dp,
                            onClick = {
                                navController.navigate("detail/${item.device.address}")
                            }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            modifier = Modifier.padding(8.dp),
                                            text = item.device.name
                                        )
                                    }
                                    Text(
                                        modifier = Modifier.padding(8.dp),
                                        text = item.device.address
                                    )
                                }
                            }
                        }

                        if (devices.value.lastIndex != index) {
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

        LaunchedEffect(key1 = Unit) {
            viewModel.startScan()
        }
    } else {
        if (doNotShowRationale) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                Text("Feature not available")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp)
            ) {
                Text("The Location and Record Audio is important for this app. Please grant the permission.")

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(modifier = Modifier.weight(0.5f),
                        onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                        Text("Ok!")
                    }

                    Spacer(Modifier.width(4.dp))

                    Button(modifier = Modifier.weight(0.5f),
                        onClick = { doNotShowRationale = true }) {
                        Text("Nope")
                    }
                }
            }
        }
    }
}