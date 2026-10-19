import Foundation
import Shared
import Testing
@testable import JustRelax

struct LibraryTextTests {
    @Test(arguments: [(8, "greeting_morning"), (13, "greeting_afternoon"), (19, "greeting_evening"), (2, "greeting_night")])
    func theGreetingFollowsTheTimeOfDay(hour: Int, key: String) {
        #expect(LibraryText.greeting(hour: hour).key == key)
    }

    @Test func aFailedDownloadIsSaidInTheAppLanguage() {
        #expect(LibraryText.message(LibraryMessageDownloadFailed.shared) == String(localized: .downloadFailed))
    }

    @Test func aTextMessageIsShownAsItIs() {
        #expect(LibraryText.message(LibraryMessageText(value: "No space left")) == "No space left")
    }
}
