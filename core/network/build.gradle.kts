plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildConfig)
    id("justrelax.koin")
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

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.okio)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.play.integrity)
            implementation(libs.kotlinx.coroutines.play.services)
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
