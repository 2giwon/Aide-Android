plugins {
    alias(libs.plugins.aide.android.library)
}

android {
    namespace = "com.giwon.aide.core.database"
}

dependencies {
    implementation(project(":core:model"))
}
