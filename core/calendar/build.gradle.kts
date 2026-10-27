plugins {
    alias(libs.plugins.aide.android.library)
    alias(libs.plugins.aide.android.hilt)
}

android {
    namespace = "com.giwon.aide.core.calendar"
}

dependencies {
    api(project(":core:model"))
    api(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
