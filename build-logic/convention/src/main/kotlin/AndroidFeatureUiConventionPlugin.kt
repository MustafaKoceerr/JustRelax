import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `justrelax.android.feature.ui`: a feature's Android screens (`:feature:x:ui`). It gets the design
 * system and its shared module (`:feature:x`) with the ViewModels, so build files only add extras.
 */
class AndroidFeatureUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("justrelax.android.library")
                apply("justrelax.android.compose")
            }

            dependencies {
                // Its routes take the shared ViewModels as parameters.
                "api"(project(path.removeSuffix(":ui")))
                "implementation"(project(":core:common"))
                "implementation"(project(":core:model"))
                "implementation"(project(":core:presentation"))
                "implementation"(project(":core:ui"))

                "implementation"(bundle("androidx-lifecycle-compose"))
                "implementation"(library("koin-androidx-compose"))

                "testImplementation"(project(":core:testing"))
                "testImplementation"(bundle("robolectric-test"))
            }
        }
    }
}
