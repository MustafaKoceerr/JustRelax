plugins {
    id("justrelax.kmp.library")
}


// Sadece test kaynak setlerinden (commonTest, androidUnitTest) bağımlılık olarak kullanılır.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
