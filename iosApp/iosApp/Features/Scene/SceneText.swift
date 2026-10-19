import Foundation
import Shared

/// The scene's one-off messages in the app's language.
enum SceneText {
    static func message(_ message: SceneMessage) -> String {
        switch onEnum(of: message) {
        case .saved(let saved): String(localized: .msgMixSavedSuccess(saved.name))
        case .emptyName: String(localized: .errMixSaveEmptyName)
        case .nameTaken: String(localized: .errMixSaveNameExists)
        case .nothingPlaying: String(localized: .errMixSaveNoSound)
        case .saveFailed: String(localized: .errMixSaveFailed)
        }
    }
}
