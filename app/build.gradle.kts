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
    compileSdk = 36

    defaultConfig {
        applicationId = "com.st.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "5.2.13"

        vectorDrawables { useSupportLibrary = true }

        manifestPlaceholders["appAuthRedirectScheme"] = "com.st.demo"
    }

    hilt {
        enableAggregatingTask = true
    }

    buildTypes {
        release {
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
        compose = true
    }


    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
        arg("room.generateKotlin", "true")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
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

    debugImplementation(libs.androidx.compose.uitestmanifest)
}
