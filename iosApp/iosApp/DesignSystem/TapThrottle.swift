/// Lets one tap through per interval, as Android throttles its play buttons.
struct TapThrottle {
    let interval: Duration
    private var lastAllowed: ContinuousClock.Instant?

    init(interval: Duration) {
        self.interval = interval
    }

    mutating func allows(at now: ContinuousClock.Instant = .now) -> Bool {
        if let lastAllowed, now - lastAllowed < interval { return false }
        lastAllowed = now
        return true
    }
}
