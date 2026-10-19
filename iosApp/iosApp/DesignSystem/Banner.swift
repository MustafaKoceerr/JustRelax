import Foundation

/// A short message at the bottom of the screen, the iOS form of Android's snackbar.
struct Banner: Identifiable {
    /// A button on the banner, such as Undo.
    struct Action {
        let title: String
        let perform: () -> Void
    }

    let id = UUID()
    let message: String
    var action: Action?

    /// How long the banner stays; one with an action stays longer so there is time to use it.
    var duration: Duration {
        action == nil ? .seconds(4) : .seconds(8)
    }
}
