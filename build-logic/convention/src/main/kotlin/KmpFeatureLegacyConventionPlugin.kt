import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * `justrelax.kmp.feature.legacy`: a feature module with its Compose Multiplatform screens. Temporary:
 * features move to `justrelax.kmp.feature` + `justrelax.android.feature.ui` one by one, then this goes.
 */
class KmpFeatureLegacyConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("justrelax.kmp.compose")

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.commonMain.dependencies {
                    implementation(project(":core:common"))
                    implementation(project(":core:model"))
                    implementation(project(":core:ui"))
                    implementation(project(":core:presentation"))
                    implementation(project(":core:data"))

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
