import Testing
@testable import JustRelax

struct SoundVolumesTests {
    @Test func aSoundThatIsNotInTheSceneIsSilent() {
        let volumes = SoundVolumes()

        #expect(volumes.level(of: "rain").value == 0)
    }

    @Test func updatesReachEachSound() {
        let volumes = SoundVolumes()

        volumes.update(["rain": 0.7, "thunder": 0.4])

        #expect(volumes.level(of: "rain").value == 0.7)
        #expect(volumes.level(of: "thunder").value == 0.4)
    }

    @Test func aSoundKeepsItsLevelObjectAcrossUpdates() {
        // Each tile watches only its own level, so the object must stay the same.
        let volumes = SoundVolumes()
        let rain = volumes.level(of: "rain")

        volumes.update(["rain": 0.5])

        #expect(volumes.level(of: "rain") === rain)
        #expect(rain.value == 0.5)
    }

    @Test func aSoundThatLeavesTheSceneFallsSilent() {
        let volumes = SoundVolumes()
        volumes.update(["rain": 0.5])

        volumes.update([:])

        #expect(volumes.level(of: "rain").value == 0)
    }
}
