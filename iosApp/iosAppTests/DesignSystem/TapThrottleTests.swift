import Testing
@testable import JustRelax

struct TapThrottleTests {
    private let start = ContinuousClock.now

    @Test func theFirstTapGoesThrough() {
        var throttle = TapThrottle(interval: .milliseconds(500))

        let allowed = throttle.allows(at: start)

        #expect(allowed)
    }

    @Test func aTapSoonAfterIsDropped() {
        var throttle = TapThrottle(interval: .milliseconds(500))
        _ = throttle.allows(at: start)

        let allowed = throttle.allows(at: start + .milliseconds(300))

        #expect(!allowed)
    }

    @Test func aTapAfterTheIntervalGoesThrough() {
        var throttle = TapThrottle(interval: .milliseconds(500))
        _ = throttle.allows(at: start)
        _ = throttle.allows(at: start + .milliseconds(300))

        let allowed = throttle.allows(at: start + .milliseconds(500))

        #expect(allowed)
    }
}
