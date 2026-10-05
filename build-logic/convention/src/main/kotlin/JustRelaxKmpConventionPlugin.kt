import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class JustRelaxKmpConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }

            val androidExtension = extensions.getByType<LibraryExtension>()
            configureAndroid(androidExtension)

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(JVM_TARGET)
                    }
                }

                if (isIosEnabled) {
                    iosX64()
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
