import Foundation
import Observation
import Shared

/// The sleep timer screen, backed by the shared TimerViewModel; it shares its timer with the
/// scene, so a timer started in one shows in the other.
@MainActor
@Observable
final class TimerModel: Identifiable {
    private(set) var state: TimerUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: TimerViewModel

    init() {
        viewModel = host.viewModel(TimerViewModel.self)
        state = viewModel.uiState.value
    }

    /// When a running timer ends, to the minute.
    var endsAt: Date? {
        state.endsAt.map { Date(timeIntervalSince1970: TimeInterval($0.epochSeconds)) }
    }

    func follow() async {
        for await state in viewModel.uiState {
            self.state = state
        }
    }

    func start(_ dial: TimerDial) {
        viewModel.startTimer(seconds: Int64(dial.totalSeconds))
    }

    func togglePause() {
        viewModel.toggleTimer()
    }

    func cancel() {
        viewModel.cancelTimer()
    }

    isolated deinit {
        host.clear()
    }
}
