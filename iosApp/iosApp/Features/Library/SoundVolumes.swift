import Observation

/// The scene's volumes, one observable level per sound, so that dragging one tile redraws only
/// that tile.
@MainActor
final class SoundVolumes {
    @MainActor
    @Observable
    final class Level {
        var value: Float = 0
    }

    private var levels: [String: Level] = [:]

    func level(of soundId: String) -> Level {
        if let level = levels[soundId] { return level }
        let level = Level()
        levels[soundId] = level
        return level
    }

    /// Sounds missing from `volumes` are not in the scene and fall silent.
    func update(_ volumes: [String: Float]) {
        for (soundId, level) in levels where volumes[soundId] == nil && level.value != 0 {
            level.value = 0
        }
        for (soundId, value) in volumes where level(of: soundId).value != value {
            level(of: soundId).value = value
        }
    }
}
