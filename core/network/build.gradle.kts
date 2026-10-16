import java.util.Properties

plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildConfig)
}

// The OpenAI key comes from local.properties and is built into the app for now; it moves to a
// server before the next release.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

buildConfig {
    packageName("com.mustafakoceerr.justrelax.core.network")
    buildConfigField("String", "CDN_BASE_URL", "\"https://cdn.mustafakoceerr.com/\"")
    buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY")}\"")
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
            implementation(project.dependencies.platform(libs.openai.bom))
            implementation(libs.openai.client)
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
