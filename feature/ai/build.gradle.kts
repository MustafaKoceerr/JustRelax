import java.util.Properties

plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildConfig)
}

// OpenAI anahtarı local.properties'ten okunur (şimdilik APK'ya gömülü; ileride backend'e taşınacak).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

buildConfig {
    packageName("com.mustafakoceerr.justrelax.feature.ai")
    buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY")}\"")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(project.dependencies.platform(libs.openai.bom))
            implementation(libs.openai.client)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMainDependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
