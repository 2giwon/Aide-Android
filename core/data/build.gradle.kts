plugins {
    alias(libs.plugins.aide.android.library)
    alias(libs.plugins.aide.android.hilt)
    alias(libs.plugins.aide.android.junit)
}

android {
    namespace = "com.giwon.aide.core.data"
}

dependencies {
    api(project(":core:model"))
    implementation(project(":core:database"))
    implementation(project(":core:calendar"))
    implementation(libs.androidx.datastore.preferences)
}
