plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization) // Model'de @Serializable var
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-serialization-json").get())

            // SoundCategory içinde ImageVector kullanıyorduk ama onu temizleyeceğiz demiştik.
            // Şimdilik temizlemeden taşıyacaksak buraya compose eklememiz gerekir.
            // AMA biz temizleyerek taşıyacağız, o yüzden compose eklemiyorum.
        }
    }
}