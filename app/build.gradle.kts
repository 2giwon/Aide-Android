plugins {
    alias(libs.plugins.aide.android.application)
    alias(libs.plugins.aide.android.compose)
    alias(libs.plugins.aide.android.hilt)
    alias(libs.plugins.aide.android.junit)
}

android {
    namespace = "com.giwon.aide"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:calendar"))
    implementation(project(":feature:settings"))
    implementation(project(":core:notification"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.timber)
    debugImplementation(libs.leakcanary.android)
}
