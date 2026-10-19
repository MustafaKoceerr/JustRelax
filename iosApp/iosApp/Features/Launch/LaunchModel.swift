import Observation
import Shared

/// Follows the shared SplashViewModel, which syncs the catalog and reads the consent answer.
@MainActor
@Observable
final class LaunchModel {
    private(set) var phase: LaunchPhase

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SplashViewModel

    init() {
        viewModel = host.viewModel(SplashViewModel.self)
        phase = LaunchPhase(viewModel.uiState.value)
    }

    /// Follows the splash until it is ready or the calling task is cancelled.
    func observe() async {
        for await state in viewModel.uiState {
            phase = LaunchPhase(state)
            if phase != .splash { return }
        }
    }

    isolated deinit {
        host.clear()
    }
}
