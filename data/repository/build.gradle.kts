/*
Anlaştık. Şimdi mimarinin orkestra şefi olan :data:repository modülünü kuruyoruz.
Sorumluluk (SRP): Bu modül, uygulamanın "Tek Doğruluk Kaynağı"dır (Single Source of Truth). :core:domain katmanındaki Repository arayüzlerini implement eder. Bunu yaparken :core:network ve :core:database modüllerini bir orkestra şefi gibi yönetir; ne zaman internetten veri çekileceğine, ne zaman veritabanına yazılacağına ve ne zaman veritabanından okunacağına karar verir.

 */
plugins {
    id("justrelax.kmp.library")
}

android {
    namespace = "com.mustafakoceerr.justrelax.data.repository"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:common"))
            implementation(project(":core:model"))

            // Datastore 1.1.0 sürümünden sonra KMP desteklemeye başladı.
            implementation(libs.findLibrary("androidx-datastore-preferences").get())
            implementation(libs.findLibrary("androidx-datastore").get())
            // DI
            implementation(libs.findLibrary("koin-core").get())
        }
    }
}