import Shared
import Testing
@testable import JustRelax

/// The editor's model on the real shared ViewModel; the test host app has started Koin.
@MainActor
struct SoundEditorModelTests {
    private func model() -> SoundEditorModel {
        SoundEditorModel(request: EditorRequest(sourcePath: "/missing/rain.mp3", suggestedName: "Rain", origin: nil))
    }

    /// The category picker opens a page of its own, which ends the editor's `.task` and so its
    /// follow of the ViewModel; the picked category must show before the ViewModel's echo.
    @Test func aPickedCategoryShowsAtOnce() {
        let model = model()

        model.category = .rain

        #expect(model.category == .rain)
    }
}
