import Foundation
import Observation
import Shared

/// The sound editor for one file, backed by the shared SoundEditorViewModel.
@MainActor
@Observable
final class SoundEditorModel {
    private(set) var state: SoundEditorUiState
    /// Kept here, as the AI prompt is, so a late echo from the ViewModel can't undo typing.
    var name: String {
        didSet { viewModel.changeName(name: name) }
    }
    /// Kept here too: the category picker opens a page of its own, which ends the view's
    /// `followState`, so the ViewModel's echo would come too late for the picker to see the choice.
    /// Only the user changes it.
    var category: SoundCategory? {
        didSet { if let category, category != oldValue { viewModel.selectCategory(category: category) } }
    }

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: SoundEditorViewModel

    init(request: EditorRequest) {
        viewModel = host.viewModel(
            SoundEditorViewModel.self,
            parameters: [request.sourcePath, request.suggestedName, request.origin ?? NSNull()]
        )
        state = viewModel.uiState.value
        name = viewModel.uiState.value.name
        category = viewModel.uiState.value.category
    }

    var kind: SoundKind {
        get { state.kind }
        set { viewModel.selectKind(kind: newValue) }
    }

    /// Follows the editor until the calling task is cancelled; messages go to `banners`, and
    /// `onSaved` runs once the sound is in the library.
    func followState(banners: BannerCenter?, onSaved: () -> Void) async {
        for await state in viewModel.uiState {
            // The ViewModel fills the name in once the file has loaded.
            if self.state.isLoading, !state.isLoading, name.isEmpty { name = state.name }
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: EditorText.message(message)))
                viewModel.onMessageShown()
            }
            if state.isSaved {
                onSaved()
                return
            }
        }
    }

    func moveStart(to ms: Int64) {
        viewModel.moveStart(toMs: ms)
    }

    func moveEnd(to ms: Int64) {
        viewModel.moveEnd(toMs: ms)
    }

    func selectIcon(_ icon: SoundIcon) {
        viewModel.selectIcon(icon: icon)
    }

    func togglePreview() {
        viewModel.togglePreview()
    }

    func save() {
        viewModel.save()
    }

    isolated deinit {
        host.clear()
    }
}
