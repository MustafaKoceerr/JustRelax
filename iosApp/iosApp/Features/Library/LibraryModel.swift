import Observation
import Shared

/// The sound library, backed by the shared LibraryViewModel.
@MainActor
@Observable
final class LibraryModel {
    private(set) var state: LibraryUiState
    @ObservationIgnored let volumes = SoundVolumes()

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: LibraryViewModel
    @ObservationIgnored private var throttle = TapThrottle(interval: .milliseconds(500))

    init() {
        viewModel = host.viewModel(LibraryViewModel.self)
        state = viewModel.uiState.value
        volumes.update(Self.floats(viewModel.volumes.value))
    }

    /// The categories that have sounds, in the Android order.
    var categories: [SoundCategory] {
        SoundCategory.allCases.filter { state.categories[$0] != nil }
    }

    var sounds: [LocalizedSound] {
        state.selectedCategory.flatMap { state.categories[$0] } ?? []
    }

    /// Follows the library until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: LibraryText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    /// Follows the volumes until the calling task is cancelled.
    func followVolumes() async {
        for await volumes in viewModel.volumes {
            self.volumes.update(Self.floats(volumes))
        }
    }

    func select(_ category: SoundCategory) {
        viewModel.selectCategory(category: category)
    }

    func toggle(_ sound: LocalizedSound) {
        guard throttle.allows() else { return }
        viewModel.toggleSound(sound: sound)
    }

    func changeVolume(of soundId: String, to volume: Float) {
        viewModel.changeVolume(soundId: soundId, volume: volume)
    }

    func changeFrequency(of soundId: String, to frequency: EventFrequency) {
        viewModel.changeFrequency(soundId: soundId, frequency: frequency)
    }

    func retry() {
        viewModel.retry()
    }

    private static func floats(_ volumes: [String: KotlinFloat]) -> [String: Float] {
        volumes.mapValues(\.floatValue)
    }

    isolated deinit {
        host.clear()
    }
}
