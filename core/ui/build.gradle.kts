plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose") // Compose'u aktif eden pluginimiz
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Alt katmanlara erişim (Model'deki AppTheme enum'ı için lazım)
            api(project(":core:common"))
            api(project(":core:model"))

            // Compose Kütüphaneleri
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // İkonlar (SoundCategory ve Tab'lar için kullanıyorsun)
            implementation(compose.materialIconsExtended)

            // Coil (Eğer ortak UI bileşenlerinde resim yüklüyorsan)
            implementation(libs.findLibrary("coil-compose").get())

            implementation(libs.findLibrary("koin-core").get())

        }

        androidMain.dependencies {
            // AndroidLanguageSwitcher içinde 'AppCompatDelegate' kullanıyorsun
            implementation(libs.findLibrary("androidx-appcompat").get())

            // Context ve Activity işlemleri için
            implementation(libs.findLibrary("androidx-activity-compose").get())
        }

        iosMain.dependencies {
            // iOS tarafında UIKit vb. zaten default geliyor
        }
    }
}

// --- Resource Ayarları ---
// Buradaki string, font ve resimlere diğer modüllerin erişebilmesi için:
compose.resources {
    publicResClass = true
    packageOfResClass = "com.mustafakoceerr.justrelax.core.ui.generated.resources"
    generateResClass = always
}