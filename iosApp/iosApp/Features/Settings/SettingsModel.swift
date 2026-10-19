import Observation
import Shared

/// Settings, backed by the shared SettingsViewModel.
@MainActor
@Observable
final class SettingsModel {
    private(set) var state: SettingsUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SettingsViewModel

    init() {
        viewModel = host.viewModel(SettingsViewModel.self)
        state = viewModel.uiState.value
    }

    var theme: AppTheme {
        get { state.theme }
        set { viewModel.changeTheme(theme: newValue) }
    }

    var isUsageDataEnabled: Bool {
        get { state.isUsageDataEnabled }
        set { viewModel.setUsageDataEnabled(enabled: newValue) }
    }

    /// Follows the settings until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: SettingsText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    /// On iOS this opens the app's page in the Settings app, where its language is chosen.
    func openLanguage() { viewModel.openLanguageSelection() }
    func downloadLibrary() { viewModel.downloadLibrary() }
    func openPrivacyPolicy() { viewModel.openPrivacyPolicy() }
    func openTerms() { viewModel.openTermsAndConditions() }
    func rateApp() { viewModel.rateApp() }
    func sendFeedback() { viewModel.sendFeedback() }

    isolated deinit {
        host.clear()
    }
}
