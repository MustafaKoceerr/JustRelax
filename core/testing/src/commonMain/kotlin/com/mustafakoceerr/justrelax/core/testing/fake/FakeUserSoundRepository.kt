package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.data.repository.UserSoundDraft
import com.mustafakoceerr.justrelax.core.data.repository.UserSoundRepository
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.model.License
import com.mustafakoceerr.justrelax.core.model.Sound
import com.mustafakoceerr.justrelax.core.model.SoundClip
import com.mustafakoceerr.justrelax.core.model.SoundSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** Keeps user sounds in memory; [createError] makes [create] fail like a failed trim. */
class FakeUserSoundRepository(initialSounds: List<Sound> = emptyList()) : UserSoundRepository {

    val sounds = MutableStateFlow(initialSounds)
    val drafts = mutableListOf<UserSoundDraft>()
    var createError: MediaException? = null

    override fun getUserSounds(): Flow<List<Sound>> = sounds

    override suspend fun create(draft: UserSoundDraft): Sound {
        drafts += draft
        createError?.let { throw it }
        val sound = Sound(
            id = "user-${drafts.size}",
            names = mapOf("en" to draft.name.trim()),
            category = draft.category,
            kind = draft.kind,
            source = SoundSource.USER,
            clips = listOf(SoundClip(null, "/user/user-${drafts.size}.m4a", draft.range.lengthMs, 1_000)),
            license = License.OWN,
            icon = draft.icon,
        )
        sounds.update { listOf(sound) + it }
        return sound
    }

    override suspend fun delete(soundId: String) {
        sounds.update { list -> list.filterNot { it.id == soundId } }
    }
}
