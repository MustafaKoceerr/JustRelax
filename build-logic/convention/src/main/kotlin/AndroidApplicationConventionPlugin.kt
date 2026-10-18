import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * `justrelax.android.application`: the Android app module with Compose. Kotlin is built into AGP 9,
 * so there is no kotlin-android plugin.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = intVersion("android-compileSdk")

                defaultConfig {
                    minSdk = intVersion("android-minSdk")
                    targetSdk = intVersion("android-targetSdk")
                }

                compileOptions {
                    sourceCompatibility = JAVA_VERSION
                    targetCompatibility = JAVA_VERSION
                }

                buildFeatures {
                    compose = true
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
        }
    }
}
