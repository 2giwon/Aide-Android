package com.giwon.aide.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("aide.android.library")
            pluginManager.apply("aide.android.compose")
            pluginManager.apply("aide.android.hilt")
            pluginManager.apply("aide.android.junit")
        }
    }
}
