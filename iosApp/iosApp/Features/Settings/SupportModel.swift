import Observation
import Shared

/// The tip jar, backed by the shared SupportViewModel.
@MainActor
@Observable
final class SupportModel {
    private(set) var state: SupportUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SupportViewModel

    init() {
        viewModel = host.viewModel(SupportViewModel.self)
        state = viewModel.uiState.value
    }

    /// Follows the tips until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: SupportText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    func tip(_ tier: TipTier) {
        viewModel.tip(tier: tier)
    }

    isolated deinit {
        host.clear()
    }
}
