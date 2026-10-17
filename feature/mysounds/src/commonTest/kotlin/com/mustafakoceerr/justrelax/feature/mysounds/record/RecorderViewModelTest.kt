package com.mustafakoceerr.justrelax.feature.mysounds.record

import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioRecorder
import com.mustafakoceerr.justrelax.core.testing.fake.FakePlaybackController
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.mysounds.generated.resources.Res
import justrelax.feature.mysounds.generated.resources.mysounds_record_failed
import justrelax.feature.mysounds.generated.resources.mysounds_record_permission_denied
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class RecorderViewModelTest {

    private val recorder = FakeAudioRecorder()
    private val playback = FakePlaybackController().apply { play() }

    private fun TestScope.viewModel() = RecorderViewModel(recorder, playback).also { observe(it.uiState) }

    @Test
    fun recordingPausesTheSceneAndCountsTimeAndLevel() = runMainTest {
        val viewModel = viewModel()

        viewModel.onPermissionResult(granted = true)
        advanceTimeBy(2.seconds)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isRecording)
        assertFalse(playback.isPlaying.value)
        assertTrue(recorder.isRecording)
        assertEquals(2_000, state.elapsedMs, "elapsed")
        assertEquals(0.5f, state.level)
    }

    @Test
    fun stoppingOpensTheEditorForTheRecording() = runMainTest {
        val viewModel = viewModel()
        viewModel.onPermissionResult(granted = true)
        advanceTimeBy(3.seconds)

        viewModel.stop()
        advanceUntilIdle()

        assertFalse(recorder.isRecording)
        assertFalse(viewModel.uiState.value.isRecording)
        assertEquals(ImportedAudio(FakeAudioRecorder.PATH, null), viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun recordingStopsByItselfAfterFiveMinutes() = runMainTest {
        val viewModel = viewModel()
        viewModel.onPermissionResult(granted = true)

        advanceTimeBy(5.minutes + 200.milliseconds)
        runCurrent()

        assertFalse(recorder.isRecording)
        assertEquals(ImportedAudio(FakeAudioRecorder.PATH, null), viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun leavingTheAppFinishesTheRecordingSoNoMicrophoneServiceIsNeeded() = runMainTest {
        val viewModel = viewModel()
        viewModel.onPermissionResult(granted = true)
        advanceTimeBy(1.seconds)

        viewModel.onAppStopped()
        advanceUntilIdle()

        assertFalse(recorder.isRecording)
        assertEquals(ImportedAudio(FakeAudioRecorder.PATH, null), viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun leavingTheAppWhileNotRecordingDoesNothing() = runMainTest {
        val viewModel = viewModel()

        viewModel.onAppStopped()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun aDeniedPermissionShowsWhyTheMicrophoneIsNeeded() = runMainTest {
        val viewModel = viewModel()

        viewModel.onPermissionResult(granted = false)
        advanceUntilIdle()

        assertFalse(recorder.isRecording)
        assertTrue(playback.isPlaying.value)
        assertEquals(UiText.Resource(Res.string.mysounds_record_permission_denied), viewModel.uiState.value.userMessage)
    }

    @Test
    fun aBusyMicrophoneOrAnEmptyRecordingShowsAMessage() = runMainTest {
        recorder.startError = MediaException("busy")
        val viewModel = viewModel()

        viewModel.onPermissionResult(granted = true)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRecording)
        assertEquals(UiText.Resource(Res.string.mysounds_record_failed), viewModel.uiState.value.userMessage)

        recorder.startError = null
        recorder.stopError = MediaException("empty")
        viewModel.onMessageShown()
        viewModel.onPermissionResult(granted = true)
        advanceTimeBy(100.milliseconds)
        viewModel.stop()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.editorToOpen)
        assertEquals(UiText.Resource(Res.string.mysounds_record_failed), viewModel.uiState.value.userMessage)
    }

    @Test
    fun discardingCancelsTheRecording() = runMainTest {
        val viewModel = viewModel()
        viewModel.onPermissionResult(granted = true)

        viewModel.discard()
        advanceUntilIdle()

        assertTrue(recorder.isCancelled)
        assertFalse(viewModel.uiState.value.isRecording)
        assertNull(viewModel.uiState.value.editorToOpen)
    }
}
