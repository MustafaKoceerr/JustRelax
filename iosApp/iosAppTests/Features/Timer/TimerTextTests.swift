import Foundation
import Testing
@testable import JustRelax

struct TimerTextTests {
    @Test(arguments: [(59, "0:59"), (754, "12:34"), (3725, "1:02:05")])
    func theCountdownShowsHoursOnlyWhenThereAreAny(seconds: Int, shown: String) {
        #expect(TimerText.countdown(seconds: seconds) == shown)
    }

    @Test func aPresetIsSaidInWords() {
        let english = Locale(identifier: "en_US")

        #expect(TimerText.preset(minutes: 15, locale: english) == "15 min")
        #expect(TimerText.preset(minutes: 90, locale: english) == "1 hr, 30 min")
    }

    @Test func theRingEmptiesAsTimePasses() {
        #expect(TimerText.progress(remaining: 450, total: 900) == 0.5)
        #expect(TimerText.progress(remaining: 0, total: 900) == 0)
        #expect(TimerText.progress(remaining: 10, total: 0) == 0)
    }
}
