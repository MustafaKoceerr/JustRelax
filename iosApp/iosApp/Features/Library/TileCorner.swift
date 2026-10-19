import Shared

/// What a sound tile shows in its top corner; the first case that applies wins, as on Android.
enum TileCorner: Equatable {
    case downloading
    case download
    /// An event sound in the scene: how often it plays, with a menu to change it.
    case frequency(EventFrequency)
    /// An event sound that isn't playing: it plays now and then.
    case event
    case none

    /// `frequency` is set only while the sound is in the scene.
    init(isDownloading: Bool, isDownloaded: Bool, kind: SoundKind, frequency: EventFrequency?) {
        self = if isDownloading {
            .downloading
        } else if !isDownloaded {
            .download
        } else if let frequency {
            .frequency(frequency)
        } else if kind == .event {
            .event
        } else {
            .none
        }
    }
}
