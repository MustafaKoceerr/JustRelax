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

            implementation(libs.findLibrary("androidx-datastore-preferences").get())
            implementation(libs.findLibrary("androidx-datastore").get())
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("okio").get())
            implementation(libs.findLibrary("sqldelight-coroutines-extensions").get())
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.findLibrary("ktor-client-mock").get())
            implementation(libs.findLibrary("okio-fakefilesystem").get())
        }

        androidHostTest.dependencies {
            implementation(libs.findLibrary("sqldelight-sqlite-driver").get())
        }
    }
}
