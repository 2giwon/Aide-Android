plugins {
    alias(libs.plugins.aide.android.library)
    alias(libs.plugins.aide.android.compose)
}

dependencies {
    api(libs.androidx.compose.material3)
}

android {
    namespace = "com.giwon.aide.core.designsystem"
}
