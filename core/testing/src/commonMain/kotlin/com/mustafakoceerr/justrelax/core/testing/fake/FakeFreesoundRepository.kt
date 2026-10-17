package com.mustafakoceerr.justrelax.core.testing.fake

import com.mustafakoceerr.justrelax.core.common.AppError
import com.mustafakoceerr.justrelax.core.data.freesound.FreesoundRepository
import com.mustafakoceerr.justrelax.core.data.freesound.FreesoundResults
import com.mustafakoceerr.justrelax.core.model.FreesoundSound

/** Answers searches from [pages] (page number to results); [error] makes searches fail. */
class FakeFreesoundRepository : FreesoundRepository {
    val pages = mutableMapOf<Int, FreesoundResults>()
    var error: AppError? = null
    var downloadSucceeds = true
    val searches = mutableListOf<Pair<String, Int>>()

    override suspend fun search(query: String, page: Int): FreesoundResults {
        searches += query to page
        error?.let { throw it }
        return pages[page] ?: FreesoundResults(emptyList(), hasMore = false)
    }

    override suspend fun downloadPreview(sound: FreesoundSound): String? =
        if (downloadSucceeds) "/incoming/freesound-${sound.id}.ogg" else null
}
