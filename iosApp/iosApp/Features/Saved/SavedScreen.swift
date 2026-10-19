import SwiftUI

/// Creates the saved mixes' model once, after the tab first appears.
struct SavedScreen: View {
    let onOpenMixer: () -> Void
    @State private var model: SavedModel?

    var body: some View {
        if let model {
            SavedView(model: model, onOpenMixer: onOpenMixer)
        } else {
            Color.clear
                .task { model = SavedModel() }
        }
    }
}
