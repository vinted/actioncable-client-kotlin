object Versions {
    const val KOTLIN = "1.2.70"
    const val ANDROID_GRADLE_PLUGIN = "3.1.4"
    const val SUPPORT_LIBRARY = "27.1.1"
    const val JUNIT = "4.12"
    const val ROBOLECTRIC = "3.7.1"
    const val MOCKITO = "2.13.0"
    const val MOCKITO_KOTILN = "1.5.0"
    const val GSON = "2.8.4"
    const val OKHTTP = "3.11.0"

    const val COMPILE_SDK_VERSION = 27
    const val MIN_SDK_VERSION = 19
    const val TARGET_SDK_VERSION = 27

    private const val MAJOR = 0
    private const val MINOR = 1
    private const val PATCH = 1

    const val VERSION_CODE: Int = (MAJOR * 10000) + (MINOR * 100) + PATCH
    const val VERSION_NAME: String = "$MAJOR.$MINOR.$PATCH"
}

object Libs {
    const val KOTLIN_STDLIB = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.KOTLIN}"
    const val APP_COMPAT_V7 = "com.android.support:appcompat-v7:${Versions.SUPPORT_LIBRARY}"
    const val JUNIT = "junit:junit:${Versions.JUNIT}"
    const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.ROBOLECTRIC}"
    const val MOCKITO = "org.mockito:mockito-core:${Versions.MOCKITO}"
    const val MOCKITO_KOTLIN = "com.nhaarman:mockito-kotlin:${Versions.MOCKITO_KOTILN}"
    const val GSON = "com.google.code.gson:gson:${Versions.GSON}"
    const val OKHTTP = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP}"
}
