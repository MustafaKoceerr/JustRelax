import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/** `justrelax.kmp.library`: a KMP module with an Android target, iOS targets when enabled, and common test libraries. */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                (this as ExtensionAware).extensions
                    .getByType<KotlinMultiplatformAndroidLibraryExtension>()
                    .configureAndroidLibrary(this@with)

                if (isIosEnabled) {
                    // No iosX64 (Intel simulator): Compose Multiplatform 1.12+ and its libraries no longer publish it.
                    iosArm64()
                    iosSimulatorArm64()
                }

                sourceSets.commonTest.dependencies {
                    implementation(kotlin("test"))
                    implementation(library("kotlinx-coroutines-test"))
                    implementation(library("turbine"))
                }
            }
        }
    }
}
