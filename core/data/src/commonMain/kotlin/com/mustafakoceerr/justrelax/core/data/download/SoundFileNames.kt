package com.mustafakoceerr.justrelax.core.data.download

/**
 * A sound's id becomes part of its clips' file names, so only ids that cannot leave the sounds
 * directory are accepted: lowercase letters, digits, `_` and `-`.
 */
internal fun isSafeSoundId(id: String): Boolean = SAFE_SOUND_ID.matches(id)

private val SAFE_SOUND_ID = Regex("^[a-z0-9_-]{1,64}$")
