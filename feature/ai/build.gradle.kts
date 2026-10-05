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
            implementation(project.dependencies.platform(libs.findLibrary("openai-bom").get()))
            implementation(libs.findLibrary("openai-client").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
        }
        androidMain.dependencies {
            implementation(libs.findLibrary("ktor-client-okhttp").get())
        }
        iosMainDependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
        }
    }
}
