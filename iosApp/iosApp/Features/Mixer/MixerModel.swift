import Observation
import Shared

/// The mixer, backed by the shared MixerViewModel: shuffle a number of sounds, lock the keepers.
@MainActor
@Observable
final class MixerModel {
    private(set) var state: MixerUiState
    @ObservationIgnored let volumes = SoundVolumes()

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: MixerViewModel
    @ObservationIgnored private var throttle = TapThrottle(interval: .milliseconds(500))

    init() {
        viewModel = host.viewModel(MixerViewModel.self)
        state = viewModel.uiState.value
        volumes.update(viewModel.volumes.value.mapValues(\.floatValue))
    }

    /// Follows the mixer until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: MixerText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    func followVolumes() async {
        for await volumes in viewModel.volumes {
            self.volumes.update(volumes.mapValues(\.floatValue))
        }
    }

    func selectCount(_ count: Int) {
        viewModel.selectSoundCount(count: Int32(count))
    }

    func shuffle() {
        viewModel.generateMix()
    }

    func toggleLock(_ soundId: String) {
        viewModel.toggleLock(soundId: soundId)
    }

    func toggle(_ soundId: String) {
        guard throttle.allows() else { return }
        viewModel.toggleSound(soundId: soundId)
    }

    func changeVolume(of soundId: String, to volume: Float) {
        viewModel.changeVolume(soundId: soundId, volume: volume)
    }

    func changeFrequency(of soundId: String, to frequency: EventFrequency) {
        viewModel.changeFrequency(soundId: soundId, frequency: frequency)
    }

    isolated deinit {
        host.clear()
    }
}
