plugins {
    alias(libs.plugins.aide.android.application)
    alias(libs.plugins.aide.android.compose)
    alias(libs.plugins.aide.android.hilt)
    alias(libs.plugins.aide.android.junit)
}

android {
    namespace = "com.giwon.aide"

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
