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

            // 1. Android Ayarları (Yazdığımız helper fonksiyonu kullanıyoruz)
            val androidExtension = extensions.getByType<LibraryExtension>()
            configureAndroid(androidExtension)

            // 2. KMP Hedefleri (Android + iOS)
            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
                    }
                }

                // iOS Hedefleri
                iosX64()
                iosArm64()
                iosSimulatorArm64()

                // Varsayılan SourceSet yapısı
                sourceSets.commonMain.dependencies {
                    // Her KMP modülünde standart olanlar
                    implementation(kotlin("stdlib"))
                }
            }
        }
    }
}