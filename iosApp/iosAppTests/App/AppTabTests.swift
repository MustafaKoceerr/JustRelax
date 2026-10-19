import Testing
import UIKit
@testable import JustRelax

struct AppTabTests {
    @Test func tabsComeInTheAndroidOrder() {
        #expect(AppTab.allCases == [.home, .mixer, .discover, .ai, .saved])
    }

    @Test(arguments: AppTab.allCases)
    func everyTabIsASymbolThatExists(tab: AppTab) {
        #expect(UIImage(systemName: tab.systemImage) != nil, "\(tab) has no symbol \(tab.systemImage)")
    }

    @Test func tabsAreFoundByTheSameIdsAsOnAndroid() {
        #expect(AppTab.allCases.map(\.accessibilityIdentifier) == ["tab_home", "tab_mixer", "tab_discover", "tab_ai", "tab_saved"])
    }
}
