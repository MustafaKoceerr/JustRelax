import Shared
import Testing
@testable import JustRelax

struct EventFrequencyTitleTests {
    @Test func everyFrequencyUsesItsAndroidTitle() {
        #expect(EventFrequency.allCases.map(\.title.key) == ["frequency_rare", "frequency_normal", "frequency_often"])
    }
}
