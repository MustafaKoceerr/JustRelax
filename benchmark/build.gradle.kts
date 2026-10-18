// Macrobenchmarks and the Baseline Profile generator. They run on a real device against
// :androidApp's benchmarkRelease build (R8 on, not debuggable), never in the unit test loop.
//   ./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest
//   ./gradlew :androidApp:generateReleaseBaselineProfile
plugins {
    alias(libs.plugins.androidTest)
    alias(libs.plugins.baselineprofile)
}

android {
    // Also the test APK's id; it must differ from the app's benchmark build (com.mustafakoceerr.justrelax.benchmark).
    namespace = "com.mustafakoceerr.justrelax.macrobenchmark"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":androidApp"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

baselineProfile {
    // Uses the connected device; there is no managed device in CI.
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.test.ext.junit)
}
