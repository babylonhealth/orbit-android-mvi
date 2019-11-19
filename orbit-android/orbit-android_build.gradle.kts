/*
 * Copyright 2019 Babylon Partners Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

apply(from = "$rootDir/gradle/scripts/bintray.gradle.kts")
apply(from = "$rootDir/gradle/scripts/codecoverage-android.gradle.kts")

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType(KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
tasks.withType(Test::class.java) {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencies {
    implementation(project(":orbit"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(ProjectDependencies.androidLifecycleComponents)
    kapt(ProjectDependencies.androidLifecycleCompiler)

    implementation(ProjectDependencies.rxJava2)
    implementation(ProjectDependencies.rxKotlin)
    implementation(ProjectDependencies.rxAndroid)

    // Testing
    GroupedDependencies.spekTestsImplementation.forEach { testImplementation(it) }
    GroupedDependencies.spekTestsRuntime.forEach { testRuntimeOnly(it) }
    testImplementation(ProjectDependencies.robolectric)
    testImplementation(ProjectDependencies.junit4)
    testRuntimeOnly(ProjectDependencies.junitVintage)
}
