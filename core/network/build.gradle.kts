plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildConfig)
}

buildConfig {
    packageName("com.mustafakoceerr.justrelax.core.network")
    buildConfigField("String", "CDN_BASE_URL", "\"https://cdn.mustafakoceerr.com/\"")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:common"))
            api(project(":core:domain"))

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMainDependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.ktor.client.mock)
        }
    }
}
