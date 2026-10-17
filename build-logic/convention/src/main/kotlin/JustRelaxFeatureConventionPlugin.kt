import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Everything a feature module needs: KMP + Compose, the shared core modules, Koin and test fakes.
 * Feature build files only declare what is specific to them.
 */
class JustRelaxFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("justrelax.kmp.library")
                apply("justrelax.android.library.compose")
            }

            val compose = extensions.getByType<ComposeExtension>().dependencies

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:common"))
                    implementation(project(":core:model"))
                    implementation(project(":core:ui"))
                    implementation(project(":core:navigation"))

                    implementation(compose.runtime)
                    implementation(compose.foundation)
                    implementation(compose.material3)
                    implementation(compose.ui)
                    implementation(compose.components.resources)
                    implementation(compose.components.uiToolingPreview)
                    implementation(compose.materialIconsExtended)

                    implementation(libs.findLibrary("koin-core").get())
                    implementation(libs.findLibrary("koin-compose").get())
                    implementation(libs.findLibrary("koin-compose-viewmodel").get())
                }

                sourceSets.commonTest.dependencies {
                    implementation(project(":core:testing"))
                }
            }
        }
    }
}
