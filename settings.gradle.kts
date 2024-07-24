/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://alphacephei.com/maven/") }
        gradlePluginPortal()
    }
}

val GPR_USER: String by settings
val GPR_API_KEY: String by settings

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        mavenLocal()

        maven {
            name = "github"
            url = uri("https://maven.pkg.github.com/SW-Platforms/BlueSTSDK_Android")
            credentials {
                username = System.getenv("GPR_USER") ?: GPR_USER
                password = System.getenv("GPR_API_KEY") ?: GPR_API_KEY
            }
        }
    }
}

rootProject.name = "STBleSDK"
include(":app")
include(":st_blue_sdk")
include(":st_opus")
