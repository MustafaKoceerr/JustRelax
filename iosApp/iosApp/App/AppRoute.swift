import Shared

/// A screen pushed onto a tab's navigation stack.
enum AppRoute: Hashable {
    case settings
    case credits
    case support
    case mySounds
    case freesound
    case soundEditor(EditorRequest)
}

/// A file to open in the sound editor; `origin` is set for a Freesound sound, to credit it.
struct EditorRequest: Hashable {
    let sourcePath: String
    let suggestedName: String
    var origin: SoundOrigin?
}
