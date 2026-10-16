import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * `justrelax.kmp.feature`: everything a feature module needs: KMP + Compose, the shared core modules,
 * Koin, lifecycle and test fakes. Feature build files only declare what is specific to them.
 */
class KmpFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("justrelax.kmp.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:common"))
                    implementation(project(":core:model"))
                    implementation(project(":core:ui"))

                    implementation(bundle("koin-compose"))
                    implementation(bundle("lifecycle-compose"))
                }

                sourceSets.commonTest.dependencies {
                    implementation(project(":core:testing"))
                }
            }
        }
    }
}
