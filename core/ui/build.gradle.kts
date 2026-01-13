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

            implementation(libs.findLibrary("koin-core").get())

            // İkonlar (SoundCategory ve Tab'lar için kullanıyorsun)
            implementation(compose.materialIconsExtended)

            // Coil (Eğer ortak UI bileşenlerinde resim yüklüyorsan)
            implementation(libs.findLibrary("coil-compose").get())

            // Coil Temel
            implementation(libs.findLibrary("coil-compose").get())

            // 1. Ktor ile internetten resim çekmek için
            implementation(libs.findLibrary("coil-network").get())
            // 2. SVG formatını desteklemek için
            implementation(libs.findLibrary("coil-svg").get())
            // 3. Ktor Motoru (Coil arka planda Ktor kullanacağı için lazım olabilir)
            implementation(libs.findLibrary("ktor-client-core").get())
        }

        androidMain.dependencies {
            // AndroidLanguageSwitcher içinde 'AppCompatDelegate' kullanıyorsun
            implementation(libs.findLibrary("androidx-appcompat").get())

            // Context ve Activity işlemleri için
            implementation(libs.findLibrary("androidx-activity-compose").get())
            implementation(libs.findLibrary("koin-android").get())

            implementation(libs.findLibrary("material3-window-size").get())
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