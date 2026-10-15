package com.giwon.aide.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class JUnitConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("de.mannodermaus.android-junit5")
            extensions.configure<CommonExtension> {
                testOptions.unitTests.isReturnDefaultValues = true
            }

            configureJunit()
            dependencies {
                add("testImplementation", libs.findLibrary("junit-jupiter-params").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("assertj-core").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
