import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform {
        excludeTags("slow")
        includeEngines("junit-jupiter")
        failFast = true
    }
}

dependencies {
    implementation(Libs.KOTLIN_STDLIB)
    implementation(Libs.KOTLIN_COROUTINES)
    implementation(Libs.OKHTTP)
    implementation(Libs.GSON)
    testImplementation(Libs.JUNIT)
    testImplementation(Libs.MOCKWEBSERVER)
}
