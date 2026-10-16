plugins {
    id("justrelax.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:common"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
