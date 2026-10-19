import Shared
import Testing
@testable import JustRelax

struct LaunchPhaseTests {
    @Test func theSplashShowsWhileTheAppGetsReady() {
        #expect(LaunchPhase(SplashUiStateLoading.shared) == .splash)
    }

    @Test func aFirstLaunchAsksForConsent() {
        #expect(LaunchPhase(SplashUiStateReady(needsConsent: true)) == .main(asksConsent: true))
    }

    @Test func anAnsweredQuestionGoesStraightToTheApp() {
        #expect(LaunchPhase(SplashUiStateReady(needsConsent: false)) == .main(asksConsent: false))
    }
}
