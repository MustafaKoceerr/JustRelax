import Foundation
import Shared

extension SoundCategory {
    /// The category's name, from the same string as on Android.
    var title: LocalizedStringResource {
        switch self {
        case .rain: .categoryRain
        case .water: .categoryWater
        case .nature: .categoryNature
        case .air: .categoryAir
        case .city: .categoryCity
        case .travel: .categoryTravel
        case .noise: .categoryNoise
        case .asmr: .categoryAsmr
        case .zen: .categoryZen
        case .music: .categoryMusic
        }
    }

    /// The SF Symbol for the category chip, close to its Material icon on Android.
    var systemImage: String {
        switch self {
        case .rain: "umbrella.fill"
        case .water: "drop.fill"
        case .nature: "tree.fill"
        case .air: "wind"
        case .city: "building.2.fill"
        case .travel: "car.fill"
        case .noise: "waveform"
        case .asmr: "headphones"
        case .zen: "figure.mind.and.body"
        case .music: "music.note"
        }
    }
}
