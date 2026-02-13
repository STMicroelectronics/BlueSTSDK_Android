package com.st.demo

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.st.demo.audio.AudioScreen
import com.st.demo.device_detail.BleDeviceDetail
import com.st.demo.device_list.BleDeviceList
import com.st.demo.feature_detail.FeatureDetail
import kotlinx.serialization.Serializable

@Serializable
data object BoardsListNavKey : NavKey

@Serializable
data class DeviceDetailNavKey(val deviceId: String) : NavKey

@Serializable
data class FeatureDetailNavKey(val deviceId: String, val featureName: String) : NavKey

@Serializable
data class DeviceAudioNavKey(val deviceId: String) : NavKey

@Composable
fun EntryProviderScope<NavKey>.audiosScreen(
    backState: NavBackStack<NavKey>
) {
    entry<DeviceAudioNavKey> { key ->
        AudioScreen(
            viewModel = hiltViewModel(),
            backStack = backState,
            deviceId = key.deviceId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.featureDetailsScreen(
    backState: NavBackStack<NavKey>
) {
    entry<FeatureDetailNavKey> { key ->
        FeatureDetail(
            viewModel = hiltViewModel(),
            backStack = backState,
            deviceId = key.deviceId,
            featureName = key.featureName
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.deviceDetailsScreen(
    backState: NavBackStack<NavKey>
) {
    entry<DeviceDetailNavKey> { key ->
        BleDeviceDetail(
            viewModel = hiltViewModel(),
            backStack = backState,
            deviceId = key.deviceId
        )
    }
}

@Composable
fun EntryProviderScope<NavKey>.devicesScreen(
    backState: NavBackStack<NavKey>
) {
    entry<BoardsListNavKey> {
        BleDeviceList(
            viewModel = hiltViewModel(),
            backStack = backState,
        )
    }
}