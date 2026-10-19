import SwiftUI

/// Creates the editor's model once for the requested file.
struct SoundEditorScreen: View {
    let request: EditorRequest
    @State private var model: SoundEditorModel?

    var body: some View {
        if let model {
            SoundEditorView(model: model)
        } else {
            Color.clear
                .task { model = SoundEditorModel(request: request) }
        }
    }
}
