import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * `justrelax.kmp.feature`: a feature's shared presentation layer (ViewModels and UI state) for Android
 * and iOS. No Compose: Android draws it in `:feature:x:ui`, iOS in SwiftUI. Feature build files only
 * declare what is specific to them.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("justrelax.kmp.library")
                apply("justrelax.koin")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:common"))
                    implementation(project(":core:model"))
                    implementation(project(":core:data"))
                    implementation(project(":core:presentation"))

                    api(library("androidx-lifecycle-viewmodel"))
                    implementation(library("koin-core-viewmodel"))
                }

                sourceSets.commonTest.dependencies {
                    implementation(project(":core:testing"))
                }
            }
        }
    }
}
