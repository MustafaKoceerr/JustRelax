import Shared
import FirebaseAnalytics
import FirebaseCore
import FirebaseCrashlytics
import Foundation

/**
 * Firebase is configured only when the build has a GoogleService-Info.plist (it is kept out of git),
 * so a clean checkout and CI build and run without sending anything.
 */
enum FirebaseReporting {
    static func configureIfAvailable() -> (AnalyticsHelper, CrashReporter)? {
        guard Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil else { return nil }
        FirebaseApp.configure()
        return (FirebaseAnalyticsHelper(), FirebaseCrashReporter())
    }
}

/**
 * Google Analytics in Consent Mode, as on Android: Info.plist turns collection and every storage
 * type off; setEnabled grants analytics storage only. Ad storage stays denied, as the app has no
 * ads, and the SDK without ad id support is linked.
 */
final class FirebaseAnalyticsHelper: AnalyticsHelper {
    func logEvent(event: AnalyticsEvent) {
        Analytics.logEvent(event.type, parameters: event.params)
    }

    func setEnabled(enabled: Bool) {
        Analytics.setConsent([
            .analyticsStorage: enabled ? .granted : .denied,
            .adStorage: .denied,
            .adUserData: .denied,
            .adPersonalization: .denied,
        ])
        Analytics.setAnalyticsCollectionEnabled(enabled)
    }
}

/** Crashlytics, off by default in Info.plist; setEnabled keeps the user's choice. */
final class FirebaseCrashReporter: CrashReporter {
    func recordException(throwable: KotlinThrowable) {
        // Kotlin's own description reads "kotlin.IllegalStateException: message".
        let description = throwable.description()
        let name = description.components(separatedBy: ":").first ?? "KotlinException"
        let model = ExceptionModel(name: name, reason: throwable.message ?? "")
        let trace = throwable.getStackTrace()
        model.stackTrace = (0..<trace.size).compactMap { index in
            (trace.get(index: index) as String?).map { StackFrame(symbol: $0, file: "", line: 0) }
        }
        Crashlytics.crashlytics().record(exceptionModel: model)
    }

    func setEnabled(enabled: Bool) {
        Crashlytics.crashlytics().setCrashlyticsCollectionEnabled(enabled)
    }
}
