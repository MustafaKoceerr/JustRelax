plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("JustRelaxDatabase") {
            packageName.set("com.mustafakoceerr.justrelax.core.database.db")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.serialization.json)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        iosMainDependencies {
            implementation(libs.sqldelight.native.driver)
        }

        androidHostTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}
