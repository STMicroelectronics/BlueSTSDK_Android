/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.benManes) apply true
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
}

fun isNonStable(version: String): Boolean {
    return version.contains("alpha", true) ||
            version.contains("beta", true) ||
            version.contains("dev", true)
}

// https://github.com/ben-manes/gradle-versions-plugin
tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

// Extend the DefaultTask class to create a CustomSTmTask class
abstract class CustomSTmTask : DefaultTask() {
    @TaskAction
    fun assembleLibraries() {
        println("All specified libraries have been assembled.")
    }

    @TaskAction
    fun publishLibrariesOnLocalMaven() {
        println("All specified libraries have been published on Local Maven.")
    }

    @TaskAction
    fun publishLibrariesToGithubPackagesRepository() {
        println("All specified libraries have been published on Github package repository.")
    }
}

// Register the Tasks with type CustomSTmTask
tasks.register<CustomSTmTask>("assembleLibraries") {
    group = "Custom STM Task"
    description = "Compiles the st_blue_sdk and st_opus libraries."

    val sdkTask = tasks.getByPath(":st_blue_sdk:assemble")
    val opusTask = tasks.getByPath(":st_opus:assemble")

    opusTask.mustRunAfter(sdkTask)

    dependsOn(sdkTask, opusTask)
}

tasks.register<CustomSTmTask>("publishLibrariesOnLocalMaven") {
    group = "Custom STM Task"
    description = "publish on local Maven the st_blue_sdk and st_opus libraries."

    val sdkTask = tasks.getByPath(":st_blue_sdk:publishToMavenLocal")
    val opusTask = tasks.getByPath(":st_opus:publishToMavenLocal")

    opusTask.mustRunAfter(sdkTask)

    dependsOn(sdkTask, opusTask)
}

tasks.register<CustomSTmTask>("publishLibrariesToGithubPackagesRepository") {
    group = "Custom STM Task"
    description = "publish on Github package the st_blue_sdk and st_opus libraries."

    val sdkTask = tasks.getByPath(":st_blue_sdk:publishBlueStSdkPublicationToGithubPackagesRepository")
    val opusTask = tasks.getByPath(":st_opus:publishBlueStSdkPublicationToGithubPackagesRepository")

    opusTask.mustRunAfter(sdkTask)

    dependsOn(sdkTask, opusTask)
}
