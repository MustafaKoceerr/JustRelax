import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** Enables Compose Multiplatform and the Compose compiler for a KMP module. */
class JustRelaxComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            // Compose resources must end up in Android assets: fonts are loaded only through the
            // AssetManager. With Android resources disabled (the AGP 9 KMP default) they are packed
            // as Java resources instead, and Font() fails at runtime.
            extensions.findByType<KotlinMultiplatformExtension>()
                ?.let { (it as ExtensionAware).extensions.findByType<KotlinMultiplatformAndroidLibraryExtension>() }
                ?.androidResources { enable = true }
        }
    }
}
