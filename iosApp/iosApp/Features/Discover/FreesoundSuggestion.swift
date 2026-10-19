import Foundation

/// The searches suggested before the first one; tapping one searches for its title.
enum FreesoundSuggestion: CaseIterable {
    case rain, thunder, ocean, birds, fireplace, cafe, wind, whiteNoise

    var title: LocalizedStringResource {
        switch self {
        case .rain: .freesoundSuggestionRain
        case .thunder: .freesoundSuggestionThunder
        case .ocean: .freesoundSuggestionOcean
        case .birds: .freesoundSuggestionBirds
        case .fireplace: .freesoundSuggestionFireplace
        case .cafe: .freesoundSuggestionCafe
        case .wind: .freesoundSuggestionWind
        case .whiteNoise: .freesoundSuggestionWhiteNoise
        }
    }

    var systemImage: String {
        switch self {
        case .rain: "drop.fill"
        case .thunder: "cloud.bolt.fill"
        case .ocean: "water.waves"
        case .birds: "bird.fill"
        case .fireplace: "fireplace.fill"
        case .cafe: "cup.and.saucer.fill"
        case .wind: "wind"
        case .whiteNoise: "waveform"
        }
    }
}
