object Versions {
    const val APP_COMPAT = "1.0.2"
    const val KOTLIN = "1.3.31"
    const val COROUTINES = "1.2.1"
    const val ANDROID_GRADLE_PLUGIN = "3.4.0"
    const val JUNIT = "5.4.2"
    const val ROBOLECTRIC = "3.7.1"
    const val MOCKITO = "2.13.0"
    const val MOCKITO_KOTILN = "1.5.0"
    const val GSON = "2.8.4"
    const val OKHTTP = "3.12.1"

    const val COMPILE_SDK_VERSION = 28
    const val MIN_SDK_VERSION = 19
    const val TARGET_SDK_VERSION = 28

    private const val MAJOR = 0
    private const val MINOR = 3
    private const val PATCH = 3

    const val VERSION_CODE: Int = (MAJOR * 10000) + (MINOR * 100) + PATCH
    const val VERSION_NAME: String = "$MAJOR.$MINOR.$PATCH"
}

object Libs {
    const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
    const val APP_COMPAT = "androidx.appcompat:appcompat:${Versions.APP_COMPAT}"
    const val KOTLIN_COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    const val JUNIT = "org.junit.jupiter:junit-jupiter:${Versions.JUNIT}"
    const val MOCKWEBSERVER = "com.squareup.okhttp3:mockwebserver:${Versions.OKHTTP}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val MOCKITO = "org.mockito:mockito-core:${Versions.MOCKITO}"
    const val MOCKITO_KOTLIN = "com.nhaarman:mockito-kotlin:${Versions.MOCKITO_KOTILN}"
    const val GSON = "com.google.code.gson:gson:${Versions.GSON}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
}
