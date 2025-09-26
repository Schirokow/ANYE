import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    kotlin("plugin.serialization") version "2.2.10"
    id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-47"
    id("com.google.gms.google-services")


}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)

            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.navigation)
            implementation(libs.koin.androidx.compose)

            // Firebase
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:34.3.0"))
            implementation("com.google.firebase:firebase-analytics")
            implementation ("com.google.firebase:firebase-firestore")
            implementation ("com.google.firebase:firebase-storage")
            implementation("com.google.firebase:firebase-auth")

            // Extended Icons
            implementation(libs.androidx.material.icons.extended)

            implementation("io.coil-kt:coil-compose:2.7.0")

        }
        commonMain.dependencies {
            implementation(project(":shared"))
            api("com.rickclephas.kmp:kmp-observableviewmodel-core:1.0.0-BETA-13")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(projects.shared)

            // Room
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)

            //OpenStreetMap
            implementation (libs.core.ktx.v1120)
            implementation (libs.play.services.location)
            implementation (libs.osmdroid.osmdroid.android)
            implementation (libs.accompanist.permissions.v0320)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.anye"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.example.anye"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    ksp(libs.room.compiler)
}

