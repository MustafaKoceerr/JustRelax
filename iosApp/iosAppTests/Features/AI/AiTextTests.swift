import Foundation
import Shared
import Testing
import UIKit
@testable import JustRelax

struct AiTextTests {
    @Test func everyProblemUsesItsAndroidText() {
        let messages: [AiMessage] = [.noSounds, .emptyResponse, .offline, .busy, .unverified, .unavailable, .unknown]

        #expect(messages.map(AiText.message) == [
            String(localized: .errAiNoSounds),
            String(localized: .errAiEmptyResponse),
            String(localized: .errAiOffline),
            String(localized: .errAiBusy),
            String(localized: .errAiUnverified),
            String(localized: .errAiUnavailable),
            String(localized: .errUnknown),
        ])
    }

    @Test func theSuggestionsAreAndroidsFour() {
        #expect(AiSuggestion.allCases.map(\.title.key) == [
            "ai_suggestion_rainforest", "ai_suggestion_deep_sleep", "ai_suggestion_cafe", "ai_suggestion_meditation",
        ])
    }

    @Test(arguments: AiSuggestion.allCases)
    func everySuggestionIsASymbolThatExists(suggestion: AiSuggestion) {
        #expect(UIImage(systemName: suggestion.systemImage) != nil)
    }
}
