import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

class JustRelaxKmpConventionPlugin : Plugin<Project> {
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

                sourceSets.commonMain.dependencies {
                    implementation(kotlin("stdlib"))
                }

                sourceSets.commonTest.dependencies {
                    implementation(kotlin("test"))
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                    implementation(libs.findLibrary("turbine").get())
                }
            }
        }
    }
}
