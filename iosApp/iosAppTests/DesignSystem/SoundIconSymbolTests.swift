import Shared
import Testing
import UIKit
@testable import JustRelax

struct SoundIconSymbolTests {
    @Test(arguments: SoundIcon.allCases)
    func everyIconIsASymbolThatExists(icon: SoundIcon) {
        #expect(UIImage(systemName: icon.systemImage) != nil, "\(icon) has no symbol \(icon.systemImage)")
    }
}
