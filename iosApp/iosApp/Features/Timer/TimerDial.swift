/// The three wheels of the timer's setup.
struct TimerDial: Equatable {
    var hours: Int
    var minutes: Int
    var seconds: Int

    init(hours: Int, minutes: Int, seconds: Int) {
        self.hours = hours
        self.minutes = minutes
        self.seconds = seconds
    }

    /// The wheels turned to a preset.
    init(minutes: Int) {
        self.init(hours: minutes / 60, minutes: minutes % 60, seconds: 0)
    }

    var totalSeconds: Int { hours * 3600 + minutes * 60 + seconds }

    var canStart: Bool { totalSeconds > 0 }

    func matches(minutes: Int) -> Bool { totalSeconds == minutes * 60 }

    /// The presets, in minutes, as on Android.
    static let presets = [15, 30, 45, 60, 90, 120]
}
