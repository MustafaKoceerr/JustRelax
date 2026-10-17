// Single source of truth: implements the :core:domain repository interfaces on top of
// :core:database (local), :core:network (remote) and DataStore (preferences).
plugins {
    id("justrelax.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            implementation(project(":core:database"))
            implementation(project(":core:network"))
            implementation(project(":core:common"))
            implementation(project(":core:model"))

            implementation(libs.androidx.datastore.preferences)
            implementation(libs.androidx.datastore)
            implementation(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.okio)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.ktor.client.mock)
            implementation(libs.okio.fakefilesystem)
        }

        androidHostTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
