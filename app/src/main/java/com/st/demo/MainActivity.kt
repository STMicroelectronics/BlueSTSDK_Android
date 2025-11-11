/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.st.demo.audio.AudioScreen
import com.st.demo.device_detail.BleDeviceDetail
import com.st.demo.device_list.BleDeviceList
import com.st.demo.feature_detail.FeatureDetail
import com.st.demo.ui.theme.StDemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT,Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT,Color.TRANSPARENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        super.onCreate(savedInstanceState)

        setContent {
            Box(Modifier.safeDrawingPadding()) {
                MainScreen()
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val navController = rememberNavController()

    StDemoTheme {
        NavHost(
            navController = navController, startDestination = "list") {

            composable(route = "list") {
                BleDeviceList(
                    viewModel = hiltViewModel(),
                    navController = navController
                )
            }

            composable(
                route = "detail/{deviceId}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    BleDeviceDetail(
                        viewModel = hiltViewModel(),
                        navController = navController,
                        deviceId = deviceId
                    )
                }
            }

            composable(
                route = "feature/{deviceId}/{featureName}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType },
                    navArgument("featureName") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    backStackEntry.arguments?.getString("featureName")?.let { featureName ->
                        FeatureDetail(
                            viewModel = hiltViewModel(),
                            navController = navController,
                            deviceId = deviceId,
                            featureName = featureName
                        )
                    }
                }
            }

            composable(
                route = "audio/{deviceId}",
                arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
            ) { backStackEntry ->
                backStackEntry.arguments?.getString("deviceId")?.let { deviceId ->
                    AudioScreen(
                        viewModel = hiltViewModel(),
                        navController = navController,
                        deviceId = deviceId
                    )
                }
            }
        }
    }
}