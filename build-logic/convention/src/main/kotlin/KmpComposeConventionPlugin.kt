import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** `justrelax.kmp.compose`: Compose Multiplatform, the Compose compiler and the common Compose libraries. */
class KmpComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("justrelax.kmp.library")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                // Compose resources must end up in Android assets: fonts are loaded only through the
                // AssetManager. With Android resources disabled (the AGP 9 KMP default) they are packed
                // as Java resources instead, and Font() fails at runtime.
                androidLibrary.androidResources { enable = true }

                sourceSets.commonMain.dependencies {
                    implementation(bundle("compose"))
                }
            }

            // Every module's Res class lives next to its code: com.mustafakoceerr.justrelax.<module>.generated.resources
            extensions.getByType<ComposeExtension>().extensions.configure<ResourcesExtension> {
                packageOfResClass = "${defaultNamespace.lowercase()}.generated.resources"
            }
        }
    }
}
