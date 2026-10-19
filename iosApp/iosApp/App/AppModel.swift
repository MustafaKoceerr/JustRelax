import Observation
import Shared

/// App-wide state from the shared MainViewModel: the theme the user picked in Settings.
@MainActor
@Observable
final class AppModel {
    private(set) var theme: AppTheme

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: MainViewModel

    init() {
        viewModel = host.viewModel(MainViewModel.self)
        theme = viewModel.theme.value
    }

    /// Follows the theme until the calling task is cancelled.
    func observe() async {
        for await theme in viewModel.theme {
            self.theme = theme
        }
    }

    isolated deinit {
        host.clear()
    }
}
