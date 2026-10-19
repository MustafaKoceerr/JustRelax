import Foundation
import Shared

/// The tip jar's message and the credits' lines.
enum SupportText {
    static func message(_ message: SupportMessage) -> String {
        switch message {
        case .purchaseFailed: String(localized: .supportFailed)
        }
    }

    /// "by author · CC BY 4.0".
    static func credit(_ credit: CreditItem) -> String {
        "\(String(localized: .creditsAuthor(credit.author))) · \(credit.license.label)"
    }
}
