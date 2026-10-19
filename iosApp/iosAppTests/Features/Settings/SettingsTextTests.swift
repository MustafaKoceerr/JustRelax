import Foundation
import Shared
import Testing
import UIKit
@testable import JustRelax

struct SettingsTextTests {
    @Test func aCompletedDownloadIsAnnounced() {
        #expect(SettingsText.message(SettingsMessageLibraryDownloaded.shared) == String(localized: .msgAllSoundsDownloaded))
        #expect(SettingsText.message(SettingsMessageText(value: "Disk full")) == "Disk full")
    }

    @Test func theThemesComeInTheAndroidOrder() {
        #expect(SettingsText.themes == [.system, .light, .dark])
        #expect(SettingsText.themes.map { SettingsText.title(of: $0).key } == ["theme_system", "theme_light", "theme_dark"])
    }

    @Test func theDownloadShowsWholePercent() {
        #expect(SettingsText.downloadProgress(0.425) == String(localized: .downloadProgress(42)))
    }

    @Test func aFailedTipSaysSo() {
        #expect(SupportText.message(.purchaseFailed) == String(localized: .supportFailed))
    }

    @Test(arguments: TipTier.allCases)
    func everyTipHasANameAndASymbolThatExists(tier: TipTier) {
        #expect(!String(localized: tier.title).isEmpty)
        #expect(UIImage(systemName: tier.systemImage) != nil)
    }

    @Test func aCreditNamesTheAuthorAndLicence() {
        let credit = CreditItem(title: "Rain", author: "mmiron", license: .ccBy40, sourceUrl: "")

        #expect(SupportText.credit(credit) == "\(String(localized: .creditsAuthor("mmiron"))) · CC BY 4.0")
    }
}
