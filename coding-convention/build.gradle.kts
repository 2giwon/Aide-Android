plugins {
    alias(libs.plugins.aide.jvm.library)
}

dependencies {
    testImplementation(libs.konsist)
}

tasks.test {
    inputs.files(
        fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**")
        }
    )
}
