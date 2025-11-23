buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
}
subprojects {
//    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
//        kotlinOptions {
//            if (project.findProperty("composeCompilerReports") == "true") {
//                kotlinOptions.freeCompilerArgs = kotlinOptions.freeCompilerArgs + listOf(
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
//                            project.buildDir.absolutePath + "/compose_reports"
//                )
//                kotlinOptions.freeCompilerArgs = kotlinOptions.freeCompilerArgs + listOf(
//                    "-P",
//                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
//                            project.buildDir.absolutePath + "/compose_metrics"
//                )
//            }
//        }
//    }
}