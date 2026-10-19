import Shared
import Testing
import UIKit
@testable import JustRelax

struct SoundCategoryDisplayTests {
    @Test(arguments: SoundCategory.allCases)
    func everyCategoryIsASymbolThatExists(category: SoundCategory) {
        #expect(UIImage(systemName: category.systemImage) != nil, "\(category) has no symbol \(category.systemImage)")
    }

    @Test(arguments: SoundCategory.allCases)
    func everyCategoryUsesItsAndroidTitle(category: SoundCategory) {
        #expect(category.title.key == "category_\(category.id.lowercased())")
    }
}
