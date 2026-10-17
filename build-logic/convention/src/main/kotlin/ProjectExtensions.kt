import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/** One JVM target for every module. */
internal val JAVA_VERSION = JavaVersion.VERSION_17
internal val JVM_TARGET = JvmTarget.JVM_17

private const val BASE_NAMESPACE = "com.mustafakoceerr.justrelax"

/** `:core:audio` -> `com.mustafakoceerr.justrelax.core.audio` */
val Project.defaultNamespace: String
    get() = BASE_NAMESPACE + path.replace(':', '.')

/**
 * iOS targets are off by default (iOS audio is not implemented yet) so Gradle sync and builds skip
 * Kotlin/Native. Enable with `justrelax.ios.enabled=true` in gradle.properties or `-P`.
 */
val Project.isIosEnabled: Boolean
    get() = providers.gradleProperty("justrelax.ios.enabled").orNull?.toBoolean() ?: false

/**
 * Adds iosMain dependencies only if iOS targets are enabled. Unlike `iosMain.dependencies {}`,
 * this does not create an orphan iosMain source set (and a warning) when iOS is turned off.
 */
fun NamedDomainObjectContainer<KotlinSourceSet>.iosMainDependencies(
    configure: KotlinDependencyHandler.() -> Unit,
) {
    matching { it.name == "iosMain" }.configureEach { dependencies(configure) }
}

/**
 * The version catalog for convention plugins. Internal and not named `libs` on purpose: a public
 * `Project.libs` here would shadow the type-safe `libs` accessors in every module build script.
 */
internal val Project.versionCatalog: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.library(alias: String): Provider<MinimalExternalModuleDependency> =
    versionCatalog.findLibrary(alias).get()

internal fun Project.bundle(alias: String) = versionCatalog.findBundle(alias).get()

internal fun Project.intVersion(alias: String): Int =
    versionCatalog.findVersion(alias).get().requiredVersion.toInt()

/** Shared settings for every KMP library's Android target (AGP 9 `com.android.kotlin.multiplatform.library`). */
internal fun KotlinMultiplatformAndroidLibraryExtension.configureAndroidLibrary(project: Project) {
    namespace = project.defaultNamespace
    compileSdk = project.intVersion("android-compileSdk")
    minSdk = project.intVersion("android-minSdk")

    // Unit tests run on the JVM (androidHostTest); Robolectric tests need merged resources.
    withHostTest {
        isIncludeAndroidResources = true
    }

    (this as? KotlinMultiplatformAndroidLibraryTarget)?.compilerOptions {
        jvmTarget.set(JVM_TARGET)
    }
}
