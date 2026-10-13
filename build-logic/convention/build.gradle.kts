plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.ksp.gradle)
    compileOnly(libs.compose.compiler.gradle)
    compileOnly(libs.hilt.gradle)
    compileOnly(libs.room.gradle)
    compileOnly(libs.android.junit5.gradle)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "aide.android.application"
            implementationClass = "com.giwon.aide.convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "aide.android.library"
            implementationClass = "com.giwon.aide.convention.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "aide.android.compose"
            implementationClass = "com.giwon.aide.convention.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "aide.android.hilt"
            implementationClass = "com.giwon.aide.convention.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "aide.android.room"
            implementationClass = "com.giwon.aide.convention.AndroidRoomConventionPlugin"
        }
        register("androidJunit") {
            id = "aide.android.junit"
            implementationClass = "com.giwon.aide.convention.JUnitConventionPlugin"
        }
        register("androidFeature") {
            id = "aide.android.feature"
            implementationClass = "com.giwon.aide.convention.AndroidFeatureConventionPlugin"
        }
    }
}
