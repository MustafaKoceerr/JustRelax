import Observation
import Shared

/// Freesound search, backed by the shared FreesoundViewModel.
@MainActor
@Observable
final class FreesoundModel {
    private(set) var state: FreesoundUiState
    /// Kept here, as the AI prompt is, so a late echo from the ViewModel can't undo typing.
    var query = "" {
        didSet { viewModel.changeQuery(query: query) }
    }

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: FreesoundViewModel

    init() {
        viewModel = host.viewModel(FreesoundViewModel.self)
        state = viewModel.uiState.value
    }

    /// Follows the search until the calling task is cancelled. Messages go to `banners`; an added
    /// sound opens in the editor through `navigator`.
    func followState(banners: BannerCenter?, navigator: AppNavigator?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: FreesoundText.message(message)))
                viewModel.onMessageShown()
            }
            if let request = state.editorToOpen {
                navigator?.push(.soundEditor(EditorRequest(sourcePath: request.path, suggestedName: request.name, origin: request.origin)))
                viewModel.onEditorOpened()
            }
        }
    }

    func search() {
        viewModel.search()
    }

    func search(_ suggestion: FreesoundSuggestion) {
        let text = String(localized: suggestion.title)
        query = text
        viewModel.searchFor(query: text)
    }

    func loadMore() {
        viewModel.loadMore()
    }

    func togglePreview(_ sound: FreesoundSound) {
        viewModel.togglePreview(sound: sound)
    }

    func stopPreview() {
        viewModel.stopPreview()
    }

    func add(_ sound: FreesoundSound) {
        viewModel.add(sound: sound)
    }

    func isPreviewing(_ sound: FreesoundSound) -> Bool {
        state.previewingId?.int64Value == sound.id
    }

    func isDownloading(_ sound: FreesoundSound) -> Bool {
        state.downloadingId?.int64Value == sound.id
    }

    isolated deinit {
        host.clear()
    }
}
