package com.mustafakoceerr.justrelax.core.model.soundscape

/** An immutable set of layers. Each sound appears at most once; at most [MAX_LAYERS] layers. */
data class Scene(val layers: List<Layer>) {

    init {
        require(layers.size <= MAX_LAYERS) { "A scene holds at most $MAX_LAYERS layers" }
        require(layers.distinctBy { it.soundId }.size == layers.size) { "Duplicate sound in scene" }
    }

    val beds: List<Layer.Bed> get() = layers.filterIsInstance<Layer.Bed>()
    val events: List<Layer.Event> get() = layers.filterIsInstance<Layer.Event>()

    fun layer(soundId: String): Layer? = layers.find { it.soundId == soundId }

    fun canAdd(layer: Layer): Boolean = layers.size < MAX_LAYERS && layer(layer.soundId) == null

    fun add(layer: Layer): Scene {
        require(canAdd(layer)) { "Cannot add ${layer.soundId}" }
        return Scene(layers + layer)
    }

    fun remove(soundId: String): Scene = Scene(layers.filterNot { it.soundId == soundId })

    fun withVolume(soundId: String, volume: Float): Scene = update(soundId) { layer ->
        val clamped = volume.coerceIn(0f, 1f)
        when (layer) {
            is Layer.Bed -> layer.copy(volume = clamped)
            is Layer.Event -> layer.copy(volume = clamped)
        }
    }

    /** Only applies to events; beds have no frequency. */
    fun withFrequency(soundId: String, frequency: EventFrequency): Scene = update(soundId) { layer ->
        if (layer is Layer.Event) layer.copy(frequency = frequency) else layer
    }

    private fun update(soundId: String, transform: (Layer) -> Layer): Scene =
        Scene(layers.map { if (it.soundId == soundId) transform(it) else it })

    companion object {
        const val MAX_LAYERS = 10
        val EMPTY = Scene(emptyList())
    }
}
