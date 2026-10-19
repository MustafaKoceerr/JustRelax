import Foundation
import Shared

extension EventFrequency {
    /// How often an event sound plays: Rare, Normal or Often.
    var title: LocalizedStringResource {
        switch self {
        case .rare: .frequencyRare
        case .normal: .frequencyNormal
        case .often: .frequencyOften
        }
    }
}
