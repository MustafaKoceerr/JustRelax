import Foundation
import Observation
import Shared

/// The user's own sounds, backed by the shared MySoundsViewModel.
@MainActor
@Observable
final class MySoundsModel {
    private(set) var state: MySoundsUiState

    @ObservationIgnored private let host = ViewModelHost()
    @ObservationIgnored private let viewModel: MySoundsViewModel

    init() {
        viewModel = host.viewModel(MySoundsViewModel.self)
        state = viewModel.uiState.value
    }

    /// The sound waiting for the delete confirmation; clearing it cancels.
    var soundToDelete: MySoundItem? {
        get { state.soundToDelete }
        set { if newValue == nil { viewModel.dismissDelete() } }
    }

    /// Follows the list until the calling task is cancelled. Messages go to `banners`; an imported
    /// file opens in the editor through `navigator`.
    func followState(banners: BannerCenter?, navigator: AppNavigator?) async {
        for await state in viewModel.uiState {
            self.state = state
            if let message = state.userMessage {
                banners?.show(Banner(message: MySoundsText.message(message)))
                viewModel.onMessageShown()
            }
            if let audio = state.editorToOpen {
                navigator?.push(.soundEditor(EditorRequest(sourcePath: audio.path, suggestedName: audio.displayName ?? "")))
                viewModel.onEditorOpened()
            }
        }
    }

    /// A file from the document picker. It is copied while access lasts, as the picker's access
    /// ends before the shared importer reads it on its own thread.
    func importFile(_ result: Result<URL, any Error>) {
        guard case .success(let url) = result, let copy = Self.copyOut(url) else {
            viewModel.import(uri: nil)
            return
        }
        viewModel.import(uri: copy.absoluteString)
    }

    func requestDelete(_ soundId: String) {
        viewModel.requestDelete(soundId: soundId)
    }

    func confirmDelete() {
        viewModel.confirmDelete()
    }

    private static func copyOut(_ url: URL) -> URL? {
        let accessed = url.startAccessingSecurityScopedResource()
        defer { if accessed { url.stopAccessingSecurityScopedResource() } }
        // The folder keeps the file's own name, which the editor suggests as the sound's name.
        let folder = URL.temporaryDirectory.appending(path: UUID().uuidString, directoryHint: .isDirectory)
        let copy = folder.appending(path: url.lastPathComponent)
        do {
            try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
            try FileManager.default.copyItem(at: url, to: copy)
            return copy
        } catch {
            return nil
        }
    }

    isolated deinit {
        host.clear()
    }
}
