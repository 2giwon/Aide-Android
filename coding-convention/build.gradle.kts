import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}
