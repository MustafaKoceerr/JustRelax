import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

// 1. TOML Dosyasına Erişim Sağlayan Extension
val Project.libs
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

// 2. Android Ayarlarını TOML'dan Okuyup Uygulayan Fonksiyon
internal fun Project.configureAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        // TOML'dan "android-compileSdk" değerini okuyoruz
        compileSdk = libs.findVersion("android-compileSdk").get().toString().toInt()

        defaultConfig {
            // TOML'dan "android-minSdk" değerini okuyoruz
            minSdk = libs.findVersion("android-minSdk").get().toString().toInt()

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        // TOML'dan "android-compileSdk" (veya targetSdk) okuyoruz
        // Not: Library modüllerde targetSdk genelde kullanılmaz ama Application için lazım olabilir.
        // Şimdilik compileSdk ile aynı tutuyoruz veya toml'daki targetSdk'yi alabiliriz.
        // LibraryExtension'da targetSdk deprecated olabilir ama CommonExtension üzerinden erişim güvenlidir.

        compileOptions {
            // Java 11 (Senin composeApp ayarın)
            sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
            targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
        }
    }
}