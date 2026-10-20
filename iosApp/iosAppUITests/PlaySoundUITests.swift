import XCTest

/**
 * The one iOS happy path: the app starts, the first-launch consent is answered and a sound from
 * the library plays, which brings up the mini player. Views are found by core:presentation's
 * TestTags, used as accessibility identifiers.
 */
final class PlaySoundUITests: XCTestCase {

    private let timeout: TimeInterval = 30

    override func setUp() {
        continueAfterFailure = false
    }

    @MainActor
    func testPlayingASoundShowsTheMiniPlayer() throws {
        let app = XCUIApplication()
        app.launch()

        // The splash decides between the consent screen (first launch) and the main screen.
        let decline = app.descendants(matching: .any)["consent_decline"]
        // The main screen is found by its tab bar, not by the tab ids: on iOS 27 SwiftUI sometimes
        // leaves the tab bar buttons without their accessibility identifiers for a whole launch.
        let tabBar = app.tabBars.firstMatch
        XCTAssertTrue(decline.waitForExistence(timeout: timeout) || tabBar.exists, "Neither consent nor the main screen appeared")
        if decline.exists { decline.tap() }
        XCTAssertTrue(tabBar.waitForExistence(timeout: timeout), "The main screen did not appear")

        // The starter pack downloads in the background, so the first sound may take a moment.
        let sound = app.descendants(matching: .any).matching(identifier: "sound_card").firstMatch
        XCTAssertTrue(sound.waitForExistence(timeout: timeout), "No sound in the library")
        sound.tap()

        let timerChip = app.descendants(matching: .any)["mini_player_timer"]
        XCTAssertTrue(timerChip.waitForExistence(timeout: timeout), "The mini player did not appear")
    }
}
