object Versions {
    const val APP_COMPAT = "1.2.0"
    const val KOTLIN = "1.4.20"
    const val COROUTINES = "1.3.2"
    const val ANDROID_GRADLE_PLUGIN = "4.1.1"
    const val JUNIT = "5.4.2"
    const val MOCKITO_KOTILN = "2.1.0"
    const val MOCKITO_INLINE = "3.0.0"
    const val GSON = "2.8.6"
    const val OKHTTP = "4.9.0"

    const val COMPILE_SDK_VERSION = 28
    const val MIN_SDK_VERSION = 19
    const val TARGET_SDK_VERSION = 28

    private const val MAJOR = 0
    private const val MINOR = 4
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
    const val MOCKITO_KOTLIN = "com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.MOCKITO_KOTILN}"
    const val MOCKITO_INLINE = "org.mockito:mockito-inline:${Versions.MOCKITO_INLINE}"
    const val GSON = "com.google.code.gson:gson:${Versions.GSON}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
}
