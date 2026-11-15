plugins {
    alias(libs.plugins.aide.android.library)
    alias(libs.plugins.aide.android.hilt)
    alias(libs.plugins.aide.android.room)
    alias(libs.plugins.aide.android.junit)
}

android {
    namespace = "com.giwon.aide.core.database"
    testOptions.unitTests.isIncludeAndroidResources = true
}

tasks.withType<Test>().configureEach {
    jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED")
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit.vintage.engine)
}
