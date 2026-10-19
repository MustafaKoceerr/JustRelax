import Foundation
import Shared

/// The mixer's message and its sound counts.
enum MixerText {
    static func message(_ message: MixerMessage) -> String {
        switch message {
        case .generateFailed: String(localized: .errMixerGenerateFailed)
        }
    }

    /// How many sounds a shuffle can pick, from the shared generator's limits.
    static let counts = Array(Int(RandomSceneGenerator.companion.MIN_LAYERS)...Int(RandomSceneGenerator.companion.MAX_LAYERS))

    /// A mix can't be smaller than the sounds locked in it.
    static func canPick(_ count: Int, minimum: Int) -> Bool {
        count >= minimum
    }
}
