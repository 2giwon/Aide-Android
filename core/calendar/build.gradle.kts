plugins {
    alias(libs.plugins.aide.android.library)
}

android {
    namespace = "com.giwon.aide.core.calendar"
}

dependencies {
    implementation(project(":core:model"))
}
