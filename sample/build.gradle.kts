import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(Versions.COMPILE_SDK_VERSION)

    defaultConfig {
        applicationId = "com.vinted.action.cable.client.sample"
        minSdkVersion(Versions.MIN_SDK_VERSION)
        targetSdkVersion(Versions.TARGET_SDK_VERSION)
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    sourceSets["main"].withConvention(KotlinSourceSet::class) {
        kotlin.srcDir("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":library"))

    implementation(Libs.KOTLIN_STDLIB)
    implementation(Libs.APP_COMPAT)
}
