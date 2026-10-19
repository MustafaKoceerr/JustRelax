import Foundation
import Shared

extension TipTier {
    var title: LocalizedStringResource {
        switch self {
        case .small: .tipSmallName
        case .medium: .tipMediumName
        case .large: .tipLargeName
        }
    }

    var systemImage: String {
        switch self {
        case .small: "cup.and.saucer.fill"
        case .medium: "birthday.cake.fill"
        case .large: "fork.knife"
        }
    }
}
