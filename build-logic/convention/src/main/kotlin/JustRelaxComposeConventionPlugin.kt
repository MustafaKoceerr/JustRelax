import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class JustRelaxComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // Pluginleri uygula
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // Android Library Extension'ı bul ve compose'u true yap
            val extension = extensions.getByType<LibraryExtension>()
            extension.apply {
                buildFeatures {
                    compose = true
                }
            }
        }
    }
}