import Shared

/// What the app shows at launch: the splash while it gets ready, then the tabs.
enum LaunchPhase: Equatable {
    case splash
    /// `asksConsent`: the analytics question was never answered, so it opens over the tabs.
    case main(asksConsent: Bool)

    init(_ state: SplashUiState) {
        self = switch onEnum(of: state) {
        case .loading: .splash
        case .ready(let ready): .main(asksConsent: ready.needsConsent)
        }
    }
}
