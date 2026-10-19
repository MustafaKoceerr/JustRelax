import Shared

extension SoundIcon {
    /// The SF Symbol for a sound, close to its Material icon on Android.
    var systemImage: String {
        switch self {
        case .rain: "cloud.rain.fill"
        case .thunder: "cloud.bolt.rain.fill"
        case .waterDrop: "drop.fill"
        case .waves: "water.waves"
        case .stream: "humidity.fill"
        case .wind: "wind"
        case .storm: "tornado"
        case .snow: "snowflake"
        case .fire: "flame.fill"
        case .fireplace: "fireplace.fill"
        case .forest: "tree.fill"
        case .leaf: "leaf.fill"
        case .grass: "camera.macro"
        case .bird: "bird.fill"
        case .insect: "ladybug.fill"
        case .animal: "pawprint.fill"
        case .night: "moon.stars.fill"
        case .city: "building.2.fill"
        case .cafe: "cup.and.saucer.fill"
        case .train: "tram.fill"
        case .car: "car.fill"
        case .plane: "airplane"
        case .boat: "sailboat.fill"
        case .noise: "waveform"
        case .fan: "fan.fill"
        case .keyboard: "keyboard.fill"
        case .asmr: "headphones"
        case .bell: "bell.fill"
        case .meditation: "figure.mind.and.body"
        case .music: "music.note"
        case .piano: "pianokeys"
        case .mic: "mic.fill"
        }
    }
}
