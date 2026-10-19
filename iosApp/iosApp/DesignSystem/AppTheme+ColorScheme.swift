import Shared
import SwiftUI

extension AppTheme {
    /// The color scheme to force, or nil to follow iOS.
    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}
