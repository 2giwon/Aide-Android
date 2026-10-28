plugins {
    alias(libs.plugins.aide.android.feature)
}

android {
    namespace = "com.giwon.aide.feature.calendar"

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.fragment.compose)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.google.material)
    implementation(libs.timber)

    testImplementation(libs.androidx.arch.core.testing)
}
