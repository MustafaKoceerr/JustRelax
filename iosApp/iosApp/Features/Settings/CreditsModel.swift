import Observation
import Shared

/// The sounds from other authors, backed by the shared CreditsViewModel.
@MainActor
@Observable
final class CreditsModel {
    private(set) var state: CreditsUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: CreditsViewModel

    init() {
        viewModel = host.viewModel(CreditsViewModel.self)
        state = viewModel.uiState.value
    }

    func follow() async {
        for await state in viewModel.uiState {
            self.state = state
        }
    }

    func openSource(_ credit: CreditItem) {
        viewModel.openSource(credit: credit)
    }

    isolated deinit {
        host.clear()
    }
}
