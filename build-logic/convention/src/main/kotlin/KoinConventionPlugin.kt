import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** `justrelax.koin`: Koin for a KMP module that defines Koin modules (koin-android for androidContext()). */
class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets.commonMain.dependencies { implementation(library("koin-core")) }
                    sourceSets.androidMain.dependencies { implementation(library("koin-android")) }
                }
            }
        }
    }
}
