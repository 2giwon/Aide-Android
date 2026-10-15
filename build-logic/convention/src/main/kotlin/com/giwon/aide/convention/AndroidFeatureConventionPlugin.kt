package com.giwon.aide.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("aide.android.library")
            pluginManager.apply("aide.android.compose")
            pluginManager.apply("aide.android.hilt")
            pluginManager.apply("aide.android.junit")

            dependencies {
                add("implementation", project(":core:model"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:designsystem"))
            }
        }
    }
}
