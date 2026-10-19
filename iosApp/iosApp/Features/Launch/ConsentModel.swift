import Observation
import Shared

/// The analytics question, backed by the shared ConsentViewModel.
@MainActor
@Observable
final class ConsentModel: Identifiable {
    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: ConsentViewModel

    init() {
        viewModel = host.viewModel(ConsentViewModel.self)
    }

    func answer(granted: Bool) {
        viewModel.answer(granted: granted)
    }

    func openPrivacyPolicy() {
        viewModel.openPrivacyPolicy()
    }

    /// Returns once the answer is stored.
    func waitUntilAnswered() async {
        for await done in viewModel.isDone where done.boolValue {
            return
        }
    }

    isolated deinit {
        host.clear()
    }
}
