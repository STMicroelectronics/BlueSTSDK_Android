/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

apply {
    from("publish.gradle")
}

android {
    namespace = "com.st.blue_sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    hilt {
        enableAggregatingTask = true
    }

    buildTypes {
        debug {
            buildConfigField(
                type = "String",
                name = "DB_BASE_URL",
                value = "\"https://s3.amazonaws.com/st_test/STBLESensor/\""
            )
            buildConfigField(
                type = "String",
                name = "BLUESTSDK_DB_BASE_URL",
                value = "\"https://raw.githubusercontent.com/STMicroelectronics/appconfig/blesensor_%s/bluestsdkv2/\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_GITHUB_DB_BASE_URL",
                value = "\"https://raw.githubusercontent.com/STMicroelectronics/appconfig/release/%s.expanded.json\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_GITHUB_DB_BASE_URL_BETA",
                value = "\"https://raw.githubusercontent.com/SW-Platforms/appconfig/release/%s.expanded.json\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_AZURE_DB_BASE_URL",
                value = "\"https://devicemodels.azure.com/%s.expanded.json\""
            )
        }

        release {
            buildConfigField(
                type = "String",
                name = "DB_BASE_URL",
                value = "\"https://s3.amazonaws.com/st_test/STBLESensor/\""
            )
            buildConfigField(
                type = "String",
                name = "BLUESTSDK_DB_BASE_URL",
                value = "\"https://raw.githubusercontent.com/STMicroelectronics/appconfig/blesensor_%s/bluestsdkv2/\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_GITHUB_DB_BASE_URL",
                value = "\"https://raw.githubusercontent.com/STMicroelectronics/appconfig/release/%s.expanded.json\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_GITHUB_DB_BASE_URL_BETA",
                value = "\"https://raw.githubusercontent.com/SW-Platforms/appconfig/release/%s.expanded.json\""
            )
            buildConfigField(
                type = "String",
                name = "DTMI_AZURE_DB_BASE_URL",
                value = "\"https://devicemodels.azure.com/%s.expanded.json\""
            )
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.generateKotlin", "true")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":st_opus"))

    implementation(libs.bundles.network)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
