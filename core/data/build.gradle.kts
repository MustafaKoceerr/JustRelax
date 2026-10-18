// The data layer: repositories over the local database, the network and DataStore.
plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            api(project(":core:media"))
            api(project(":core:analytics"))

            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.datastore)
            implementation(libs.ktor.client.core)
            implementation(libs.okio)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            // App language through AppCompat's per-app locales
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.ktor.client.mock)
            implementation(libs.okio.fakefilesystem)
        }

        androidHostTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
