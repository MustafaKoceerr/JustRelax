import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * `justrelax.android.library`: an Android-only library (the Compose design system and each feature's
 * `:ui` module). Kotlin is built into AGP 9, so there is no kotlin-android plugin.
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")

            extensions.configure<LibraryExtension> {
                namespace = defaultNamespace
                compileSdk = intVersion("android-compileSdk")

                defaultConfig {
                    minSdk = intVersion("android-minSdk")
                }

                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }

                // Robolectric tests need the merged resources.
                testOptions {
                    unitTests.isIncludeAndroidResources = true
                }
            }

            extensions.configure<KotlinAndroidProjectExtension> {
                compilerOptions {
                    jvmTarget.set(JVM_TARGET)
                }
            }

            dependencies {
                "testImplementation"(kotlin("test"))
                "testImplementation"(library("kotlinx-coroutines-test"))
                "testImplementation"(library("turbine"))
            }
        }
    }
}
