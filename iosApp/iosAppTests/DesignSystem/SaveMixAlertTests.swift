import Testing
@testable import JustRelax

struct SaveMixAlertTests {
    @Test(arguments: ["", "   ", "\n"])
    func aBlankNameCannotBeSaved(name: String) {
        #expect(!SaveMixAlert.canSave(name))
    }

    @Test func anyOtherNameCanBeSaved() {
        #expect(SaveMixAlert.canSave("Evening rain"))
        #expect(SaveMixAlert.canSave("  Focus "))
    }
}
