import Foundation
import Observation
import Shared

/// The saved mixes, backed by the shared SavedViewModel.
@MainActor
@Observable
final class SavedModel {
    private(set) var state: SavedUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SavedViewModel
    @ObservationIgnored private var throttle = TapThrottle(interval: .milliseconds(500))

    init() {
        viewModel = host.viewModel(SavedViewModel.self)
        state = viewModel.uiState.value
    }

    /// The mix shown on the right of a wide window; setting it tells the shared ViewModel.
    var selectedId: Int64? {
        get { state.selected?.id }
        set { viewModel.selectMix(mixId: newValue.map(KotlinLong.init(value:))) }
    }

    /// Follows the mixes until the calling task is cancelled; messages go to `banners`, a deleted
    /// mix's with Undo.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.message {
                var banner = Banner(message: SavedText.message(message))
                if case .deleted = onEnum(of: message) {
                    banner.action = Banner.Action(title: String(localized: .actionUndo)) { [weak self] in
                        self?.viewModel.undoDelete()
                    }
                }
                banners?.show(banner)
                viewModel.onMessageShown()
            }
        }
    }

    func isPlaying(_ mixId: Int64) -> Bool {
        state.playingMixId?.int64Value == mixId
    }

    func togglePlay(_ mixId: Int64) {
        guard throttle.allows() else { return }
        viewModel.togglePlay(mixId: mixId)
    }

    func delete(_ mixId: Int64) {
        viewModel.deleteMix(mixId: mixId)
    }

    static func date(_ instant: KotlinInstant) -> Date {
        Date(timeIntervalSince1970: TimeInterval(instant.epochSeconds))
    }

    isolated deinit {
        host.clear()
    }
}
