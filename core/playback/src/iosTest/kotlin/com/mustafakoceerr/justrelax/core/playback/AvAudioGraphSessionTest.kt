package com.mustafakoceerr.justrelax.core.playback

import com.mustafakoceerr.justrelax.core.playback.player.AvAudioGraph
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryAmbient
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The order Apple advises for an engine-based player: the session is in the playback category
 * before the engine is built, and active before the engine starts.
 */
@OptIn(ExperimentalForeignApi::class)
class AvAudioGraphSessionTest {

    @Test
    fun buildingTheGraph_putsTheSessionInThePlaybackCategory() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryAmbient, error = null)

        AvAudioGraph()

        assertEquals(AVAudioSessionCategoryPlayback, session.category)
    }

    @Test
    fun theSessionIsActivated_beforeTheEngineStarts() {
        var engineRunningWhenActivated: Boolean? = null
        lateinit var graph: AvAudioGraph
        graph = AvAudioGraph(activateSession = { engineRunningWhenActivated = graph.engine.running; true })

        graph.start()

        assertEquals(false, engineRunningWhenActivated, "the session was never activated, or only after the engine started")
        graph.engine.stop()
    }
}
