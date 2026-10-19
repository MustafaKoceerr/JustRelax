import Observation
import Shared
import SwiftUI

/// The sounds playing now, backed by the shared SceneViewModel: the mini player, the scene sheet
/// and the save question all read it.
@MainActor
@Observable
final class SceneModel {
    private(set) var state: SceneUiState
    private(set) var miniPlayer: MiniPlayerUiState
    /// Seconds left on the sleep timer while it runs or is paused.
    private(set) var timerRemaining: Int?
    @ObservationIgnored let volumes = SoundVolumes()

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SceneViewModel
    @ObservationIgnored private var playThrottle = TapThrottle(interval: .milliseconds(500))
    @ObservationIgnored private var stopThrottle = TapThrottle(interval: .seconds(1))

    init() {
        viewModel = host.viewModel(SceneViewModel.self)
        state = viewModel.uiState.value
        miniPlayer = viewModel.miniPlayer.value
        timerRemaining = viewModel.timerRemaining.value?.intValue
        volumes.update(viewModel.volumes.value.mapValues(\.floatValue))
    }

    /// Whether the save question shows; closing it tells the shared ViewModel.
    var isSaveQuestionShown: Bool {
        get { state.isSaveDialogVisible }
        set { if !newValue { viewModel.dismissSaveDialog() } }
    }

    /// Follows the scene until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: SceneText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    func followMiniPlayer() async {
        for await miniPlayer in viewModel.miniPlayer {
            self.miniPlayer = miniPlayer
        }
    }

    func followTimer() async {
        for await remaining in viewModel.timerRemaining {
            timerRemaining = remaining?.intValue
        }
    }

    func followVolumes() async {
        for await volumes in viewModel.volumes {
            self.volumes.update(volumes.mapValues(\.floatValue))
        }
    }

    func togglePlayPause() {
        guard playThrottle.allows() else { return }
        viewModel.togglePlayPause()
    }

    func stopAll() {
        guard stopThrottle.allows() else { return }
        viewModel.stopAll()
    }

    func changeVolume(of soundId: String, to volume: Float) {
        viewModel.changeVolume(soundId: soundId, volume: volume)
    }

    func changeFrequency(of soundId: String, to frequency: EventFrequency) {
        viewModel.changeFrequency(soundId: soundId, frequency: frequency)
    }

    func remove(_ soundId: String) {
        viewModel.remove(soundId: soundId)
    }

    func startTimer(minutes: Int) {
        viewModel.startTimer(seconds: Int64(minutes * 60))
    }

    func cancelTimer() {
        viewModel.cancelTimer()
    }

    /// `suggestedName` fills the name field, such as an AI mix's name.
    func askToSave(suggestedName: String? = nil) {
        viewModel.openSaveDialog(suggestedName: suggestedName)
    }

    func save(name: String) {
        viewModel.saveMix(name: name)
    }

    isolated deinit {
        host.clear()
    }
}

extension EnvironmentValues {
    /// The app's scene, for screens that save what plays; nil until the tabs create it.
    @Entry var scene: SceneModel?
}
