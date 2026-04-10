/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.jaredsburrowsLicense)
}

android {
    namespace = "com.st.demo"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.st.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 16
        versionName = "5.3.2"

        vectorDrawables { useSupportLibrary = true }

        manifestPlaceholders["appAuthRedirectScheme"] = "com.st.demo"
    }


    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

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
        compose = true
    }


    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

hilt {
    enableAggregatingTask = true
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

licenseReport {
    // Generate reports
    generateCsvReport = false
    generateHtmlReport = true
    generateJsonReport = false
    generateTextReport = false

    // Copy reports - These options are ignored for Java projects
    copyCsvReportToAssets = false
    copyHtmlReportToAssets = true
    copyJsonReportToAssets = false
    copyTextReportToAssets = false
    useVariantSpecificAssetDirs = false

    // Ignore licenses for certain artifact patterns
    //ignoredPatterns = []

    // Show versions in the report - default is false
    showVersions = true
}

dependencies {
    // Blue ST SDK
    implementation(project(":st_blue_sdk"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.composeUiTooling)
    implementation(libs.bundles.network)
    implementation(libs.androidx.material2)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata)
}
