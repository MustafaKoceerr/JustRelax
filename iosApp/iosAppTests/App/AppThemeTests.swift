import Shared
import SwiftUI
import Testing
@testable import JustRelax

struct AppThemeTests {
    @Test func theSystemThemeLeavesTheColorSchemeToIOS() {
        #expect(AppTheme.system.colorScheme == nil)
    }

    @Test func lightAndDarkAreForced() {
        #expect(AppTheme.light.colorScheme == .light)
        #expect(AppTheme.dark.colorScheme == .dark)
    }
}
