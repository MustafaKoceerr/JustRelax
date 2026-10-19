import Observation
import Shared

/// The AI mix, backed by the shared AiViewModel.
@MainActor
@Observable
final class AiModel {
    private(set) var state: AiUiState
    /// Kept here rather than read back from the ViewModel, whose echo could arrive after newer
    /// typing and undo it.
    var prompt = "" {
        didSet { viewModel.updatePrompt(text: prompt) }
    }
    @ObservationIgnored let volumes = SoundVolumes()

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: AiViewModel
    @ObservationIgnored private var throttle = TapThrottle(interval: .milliseconds(500))

    init() {
        viewModel = host.viewModel(AiViewModel.self)
        state = viewModel.uiState.value
        volumes.update(viewModel.volumes.value.mapValues(\.floatValue))
    }

    /// The notice that the prompt goes to OpenAI; closing it declines for now.
    var isDataNoticeShown: Bool {
        get { state.showAiDataNotice }
        set { if !newValue { viewModel.dismissAiDataNotice() } }
    }

    var canSend: Bool {
        !prompt.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !state.isLoading
    }

    /// Follows the AI screen until the calling task is cancelled; messages go to `banners`.
    func followState(banners: BannerCenter?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: AiText.message(message)))
                viewModel.onMessageShown()
            }
        }
    }

    func followVolumes() async {
        for await volumes in viewModel.volumes {
            self.volumes.update(volumes.mapValues(\.floatValue))
        }
    }

    func send() {
        viewModel.generateMix()
    }

    func send(_ suggestion: AiSuggestion) {
        let text = String(localized: suggestion.title)
        prompt = text
        viewModel.sendSuggestion(text: text)
    }

    func tryAnotherVariation() {
        prompt = state.lastPrompt
        viewModel.regenerate()
    }

    func startOver() {
        prompt = ""
        viewModel.clearMix()
    }

    func acceptDataNotice() {
        viewModel.acceptAiDataNotice()
    }

    func openPrivacyPolicy() {
        viewModel.openPrivacyPolicy()
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
