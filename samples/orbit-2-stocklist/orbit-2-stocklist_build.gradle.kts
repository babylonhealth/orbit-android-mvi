/*
 * Copyright 2020 Babylon Partners Limited
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

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        applicationId = "com.babylon.orbit2.sample.stocklist"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        coreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
    }

    testOptions.unitTests.isIncludeAndroidResources = true

    packagingOptions {
        pickFirst("build.number")
        pickFirst("version.number")
        pickFirst("compatibility_version.number")
        exclude("META-INF/INDEX.LIST")
        exclude("META-INF/io.netty.versions.properties")
    }
}

repositories {
    jcenter()
    maven { setUrl("https://www.lightstreamer.com/repo/maven") }
    maven { setUrl("https://dl.bintray.com/lisawray/maven") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":orbit-2-core"))
    implementation(project(":orbit-2-livedata"))
    implementation(project(":orbit-2-viewmodel"))
    implementation(project(":orbit-2-coroutines"))

    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("com.google.android.material:material:1.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.0")
    implementation("com.lightstreamer:ls-android-client:4.2.1")
    implementation("com.xwray:groupie:2.8.1")
    implementation("com.xwray:groupie-kotlin-android-extensions:2.8.1")
    implementation("com.xwray:groupie-viewbinding:2.8.1")
    implementation("org.koin:koin-androidx-viewmodel:2.1.6")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.2.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:${Versions.desugar}")
}
