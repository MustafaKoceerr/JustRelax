import SwiftUI

/// Creates the library's model once, after the tab first appears, so redrawing the tab bar never
/// starts a second shared ViewModel.
struct LibraryScreen: View {
    @State private var model: LibraryModel?

    var body: some View {
        if let model {
            LibraryView(model: model)
        } else {
            Color.clear
                .task { model = LibraryModel() }
        }
    }
}
