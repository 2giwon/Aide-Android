plugins {
    alias(libs.plugins.aide.android.library)
}

android {
    namespace = "com.giwon.aide.core.notification"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))
}
