plugins {
    alias(libs.plugins.aide.android.library)
}

android {
    namespace = "com.giwon.aide.core.data"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:calendar"))
}
