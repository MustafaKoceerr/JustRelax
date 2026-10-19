import Foundation
import Shared

/// Settings' messages, theme names and download progress.
enum SettingsText {
    static func message(_ message: SettingsMessage) -> String {
        switch onEnum(of: message) {
        case .libraryDownloaded: String(localized: .msgAllSoundsDownloaded)
        case .text(let text): text.value
        }
    }

    static let themes: [AppTheme] = [.system, .light, .dark]

    static func title(of theme: AppTheme) -> LocalizedStringResource {
        switch theme {
        case .system: .themeSystem
        case .light: .themeLight
        case .dark: .themeDark
        }
    }

    /// "Downloading… 42%".
    static func downloadProgress(_ fraction: Float) -> String {
        String(localized: .downloadProgress(Int(fraction * 100)))
    }
}
