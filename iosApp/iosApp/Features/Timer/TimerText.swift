import Foundation

/// How the sleep timer's times read.
enum TimerText {
    /// "m:ss", or "h:mm:ss" from an hour up, as on Android.
    static func countdown(seconds: Int) -> String {
        let hours = seconds / 3600
        let minutes = seconds / 60 % 60
        let rest = seconds % 60
        return if hours > 0 {
            String(format: "%d:%02d:%02d", hours, minutes, rest)
        } else {
            String(format: "%d:%02d", minutes, rest)
        }
    }

    /// A preset's length in words, such as "1 hr, 30 min", in the user's language.
    static func preset(minutes: Int, locale: Locale = .autoupdatingCurrent) -> String {
        Duration.seconds(minutes * 60).formatted(
            .units(allowed: [.hours, .minutes], width: .abbreviated).locale(locale)
        )
    }

    /// How full the ring is: 1 at the start, 0 when the time is up.
    static func progress(remaining: Int, total: Int) -> Double {
        guard total > 0 else { return 0 }
        return min(max(Double(remaining) / Double(total), 0), 1)
    }
}
