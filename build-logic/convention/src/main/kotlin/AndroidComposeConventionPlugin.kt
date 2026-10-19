import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/** `justrelax.android.compose`: androidx Compose (the BOM and the common libraries) for an Android module. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            extensions.getByType(CommonExtension::class.java).buildFeatures.compose = true

            configureComposeCompiler()

            dependencies {
                val bom = platform(library("androidx-compose-bom"))
                "implementation"(bom)
                "implementation"(bundle("androidx-compose"))
                "debugImplementation"(library("androidx-compose-ui-tooling"))
                "testImplementation"(bom)
            }
        }
    }
}
