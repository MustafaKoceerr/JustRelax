import Testing
@testable import JustRelax

struct TimerDialTests {
    @Test func theWheelsAddUpToSeconds() {
        #expect(TimerDial(hours: 1, minutes: 2, seconds: 3).totalSeconds == 3723)
    }

    @Test func aPresetTurnsTheWheels() {
        #expect(TimerDial(minutes: 90) == TimerDial(hours: 1, minutes: 30, seconds: 0))
    }

    @Test func aPresetIsSelectedWhileTheWheelsShowIt() {
        #expect(TimerDial(hours: 0, minutes: 45, seconds: 0).matches(minutes: 45))
        #expect(!TimerDial(hours: 0, minutes: 45, seconds: 1).matches(minutes: 45))
    }

    @Test func nothingToStartAtZero() {
        #expect(!TimerDial(hours: 0, minutes: 0, seconds: 0).canStart)
        #expect(TimerDial(hours: 0, minutes: 0, seconds: 1).canStart)
    }
}
