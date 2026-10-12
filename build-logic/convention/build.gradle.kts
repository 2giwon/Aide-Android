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
