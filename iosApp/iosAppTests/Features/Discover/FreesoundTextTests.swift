import Foundation
import Shared
import Testing
import UIKit
@testable import JustRelax

struct FreesoundTextTests {
    @Test func everyProblemUsesItsAndroidText() {
        let messages: [FreesoundMessage] = [.rateLimited, .offline, .unverified, .searchFailed, .downloadFailed]

        #expect(messages.map(FreesoundText.message) == [
            String(localized: .freesoundRateLimited),
            String(localized: .freesoundOffline),
            String(localized: .freesoundUnverified),
            String(localized: .freesoundFailed),
            String(localized: .freesoundDownloadFailed),
        ])
    }

    @Test func aResultCreditsItsAuthorWithLicenceAndLength() {
        let sound = FreesoundSound(
            id: 1, name: "Rain on a tent", author: "mmiron", license: .ccBy40,
            pageUrl: "", tags: [], durationMs: 42_000, previewUrl: ""
        )

        #expect(FreesoundText.details(of: sound) == "\(String(localized: .freesoundBy("mmiron"))) · CC BY 4.0 · 0:42")
    }

    @Test func theSuggestionsAreAndroidsEight() {
        #expect(FreesoundSuggestion.allCases.map(\.title.key) == [
            "freesound_suggestion_rain", "freesound_suggestion_thunder", "freesound_suggestion_ocean",
            "freesound_suggestion_birds", "freesound_suggestion_fireplace", "freesound_suggestion_cafe",
            "freesound_suggestion_wind", "freesound_suggestion_white_noise",
        ])
    }

    @Test(arguments: FreesoundSuggestion.allCases)
    func everySuggestionIsASymbolThatExists(suggestion: FreesoundSuggestion) {
        #expect(UIImage(systemName: suggestion.systemImage) != nil)
    }
}
