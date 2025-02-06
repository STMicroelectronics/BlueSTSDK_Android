/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo.device_detail

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.st.blue_sdk.models.NodeState
import com.st.demo.R

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceDetail(
    navController: NavHostController,
    viewModel: BleDeviceDetailViewModel,
    deviceId: String
) {
    LaunchedEffect(key1 = deviceId) {
        viewModel.connect(deviceId = deviceId)
    }

    val bleDevice = viewModel.bleDevice(deviceId = deviceId).collectAsState(null)
    val features = viewModel.features.collectAsState()

    if (bleDevice.value?.connectionStatus?.current == NodeState.Ready) {
        viewModel.getFeatures(deviceId = deviceId)
    }

    val backHandlingEnabled by remember { mutableStateOf(true) }

    BackHandler(enabled = backHandlingEnabled) {
        viewModel.disconnect(deviceId = deviceId)

        navController.popBackStack()
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .scrollable(
                state = scrollState,
                orientation = Orientation.Vertical
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewModel.showAudioBtn(deviceId)) {
                Text(stringResource(R.string.st_testAudio))
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    enabled = bleDevice.value?.connectionStatus?.current?.equals(NodeState.Ready)
                        ?: false,
                    onClick = {
                        navController.navigate("audio/${deviceId}")
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                }
            }
        }

        Text("Name: ${bleDevice.value?.device?.name ?: ""}")

        Spacer(modifier = Modifier.height(4.dp))

        Text("Status: ${bleDevice.value?.connectionStatus?.current?.name?.uppercase() ?: ""}")

        Spacer(modifier = Modifier.height(4.dp))

        Text("Features: ")

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(4.dp)
        ) {
            val items = features.value.filter { it.isDataNotifyFeature }
            itemsIndexed(items = items) { index, item ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth().padding(bottom = 4.dp,top = 4.dp)
                        .clickable {
                            navController.navigate("feature/${deviceId}/${item.name}")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(color = 0xFFF7F8FA) /*Grey1*/),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(8.dp),
                        text = item.name,
                    )
                }
                if (items.lastIndex != index) {
                    HorizontalDivider()
                }
            }
        }
    }
}
