plugins {
    `kotlin-dsl`
}

group = "com.mustafakoceerr.justrelax.buildlogic"

dependencies {
    // Plugin APIs used by the convention plugins (compile only: the build already loads them).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.composeCompiler.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "justrelax.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("kmpFeature") {
            id = "justrelax.kmp.feature"
            implementationClass = "KmpFeatureConventionPlugin"
        }
        register("koin") {
            id = "justrelax.koin"
            implementationClass = "KoinConventionPlugin"
        }
        register("androidApplication") {
            id = "justrelax.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "justrelax.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "justrelax.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeatureUi") {
            id = "justrelax.android.feature.ui"
            implementationClass = "AndroidFeatureUiConventionPlugin"
        }
    }
}
