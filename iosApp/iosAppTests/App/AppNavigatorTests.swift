import Testing
@testable import JustRelax

struct AppNavigatorTests {
    private let editor = AppRoute.soundEditor(EditorRequest(sourcePath: "/tmp/rain.ogg", suggestedName: "Rain"))

    @Test func theAppStartsOnTheSoundsTabWithNothingPushed() {
        let navigator = AppNavigator()

        #expect(navigator.selected == .home)
        #expect(navigator.paths.isEmpty)
    }

    @Test func aRouteIsPushedOnTheTabInView() {
        let navigator = AppNavigator()
        navigator.selected = .discover

        navigator.push(editor)

        #expect(navigator.paths[.discover] == [editor])
        #expect(navigator.paths[.home] == nil)
    }

    @Test func goingBackDropsTheTopRoute() {
        let navigator = AppNavigator()
        navigator.push(.freesound)
        navigator.push(editor)

        navigator.pop()

        #expect(navigator.paths[.home] == [.freesound])
    }
}
