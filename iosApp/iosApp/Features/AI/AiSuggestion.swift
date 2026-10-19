import Foundation

/// The four ideas on the AI's start screen; tapping one sends its title as the prompt.
enum AiSuggestion: CaseIterable {
    case rainforest, deepSleep, cafe, meditation

    var title: LocalizedStringResource {
        switch self {
        case .rainforest: .aiSuggestionRainforest
        case .deepSleep: .aiSuggestionDeepSleep
        case .cafe: .aiSuggestionCafe
        case .meditation: .aiSuggestionMeditation
        }
    }

    var systemImage: String {
        switch self {
        case .rainforest: "tree.fill"
        case .deepSleep: "moon.zzz.fill"
        case .cafe: "cup.and.saucer.fill"
        case .meditation: "figure.mind.and.body"
        }
    }
}
