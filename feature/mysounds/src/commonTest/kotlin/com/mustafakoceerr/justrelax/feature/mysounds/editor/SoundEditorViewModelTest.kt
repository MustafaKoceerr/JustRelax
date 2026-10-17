package com.mustafakoceerr.justrelax.feature.mysounds.editor

import com.mustafakoceerr.justrelax.core.data.repository.UserSoundDraft
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.media.TrimRange
import com.mustafakoceerr.justrelax.core.model.SoundCategory
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeClipPreviewer
import com.mustafakoceerr.justrelax.core.testing.fake.FakeUserSoundRepository
import com.mustafakoceerr.justrelax.core.testing.fake.FakeWaveformReader
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.ui.util.UiText
import justrelax.feature.mysounds.generated.resources.Res
import justrelax.feature.mysounds.generated.resources.mysounds_save_failed
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SoundEditorViewModelTest {

    private val reader = FakeWaveformReader(durationMs = 60_000)
    private val previewer = FakeClipPreviewer()
    private val repository = FakeUserSoundRepository()

    private fun TestScope.viewModel(name: String? = "Kettle boiling.m4a") =
        SoundEditorViewModel("/cache/import/audio", name, reader, previewer, repository).also {
            observe(it.uiState)
            advanceUntilIdle()
        }

    @Test
    fun aLongFileLoadsAsABedOverItsWholeLengthNamedAfterTheFile() = runMainTest {
        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(60_000, state.sourceMs)
        assertEquals(SoundKind.BED, state.kind)
        assertEquals(setOf(SoundKind.BED, SoundKind.EVENT), state.allowedKinds)
        assertEquals(TrimRange(0, 60_000), state.range)
        assertEquals("Kettle boiling", state.name)
        assertTrue(state.peaks.isNotEmpty())
    }

    @Test
    fun aShortFileLoadsAsAnEventThatCannotBeABed() = runMainTest {
        reader.durationMs = 4_000

        val state = viewModel(name = null).uiState.value

        assertEquals(SoundKind.EVENT, state.kind)
        assertEquals(setOf(SoundKind.EVENT), state.allowedKinds)
        assertEquals("", state.name)
    }

    @Test
    fun aFileThatCannotBeDecodedShowsTheLoadError() = runMainTest {
        reader.error = MediaException("not audio")

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertTrue(state.loadFailed)
    }

    @Test
    fun theHandlesAndTheKindFollowTheTrimRules() = runMainTest {
        val viewModel = viewModel()

        viewModel.selectKind(SoundKind.EVENT)
        advanceUntilIdle()
        assertEquals(TrimRange(0, 10_000), viewModel.uiState.value.range)

        viewModel.moveEnd(30_000)
        advanceUntilIdle()
        assertEquals(TrimRange(0, 10_000), viewModel.uiState.value.range)

        viewModel.moveStart(4_000)
        advanceUntilIdle()
        viewModel.moveEnd(6_000)
        advanceUntilIdle()
        assertEquals(TrimRange(4_000, 6_000), viewModel.uiState.value.range)

        viewModel.selectKind(SoundKind.BED)
        advanceUntilIdle()
        assertEquals(TrimRange(4_000, 9_000), viewModel.uiState.value.range)
        assertNull(viewModel.uiState.value.trimProblem)
    }

    @Test
    fun theKindCannotBeSwitchedToOneTheFileIsTooShortFor() = runMainTest {
        reader.durationMs = 4_000
        val viewModel = viewModel()

        viewModel.selectKind(SoundKind.BED)
        advanceUntilIdle()

        assertEquals(SoundKind.EVENT, viewModel.uiState.value.kind)
    }

    @Test
    fun thePreviewLoopsABedPlaysAnEventOnceAndStopsWhenTheRangeChanges() = runMainTest {
        val viewModel = viewModel()

        viewModel.togglePreview()
        advanceUntilIdle()
        assertEquals(FakeClipPreviewer.Preview("/cache/import/audio", TrimRange(0, 60_000), loop = true), previewer.lastPreview)
        assertTrue(viewModel.uiState.value.isPreviewing)

        viewModel.moveStart(1_000)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isPreviewing)

        viewModel.selectKind(SoundKind.EVENT)
        advanceUntilIdle()
        viewModel.togglePreview()
        advanceUntilIdle()
        assertEquals(FakeClipPreviewer.Preview("/cache/import/audio", TrimRange(1_000, 11_000), loop = false), previewer.lastPreview)

        viewModel.togglePreview()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isPreviewing)
    }

    @Test
    fun savingNeedsANameAndACategory() = runMainTest {
        val viewModel = viewModel()
        assertFalse(viewModel.uiState.value.canSave)

        viewModel.selectCategory(SoundCategory.CITY)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canSave)

        viewModel.changeName("   ")
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun aLongNameIsCut() = runMainTest {
        val viewModel = viewModel()

        viewModel.changeName("x".repeat(100))
        advanceUntilIdle()

        assertEquals(SoundEditorUiState.MAX_NAME_LENGTH, viewModel.uiState.value.name.length)
    }

    @Test
    fun savingStopsThePreviewAndCreatesTheSoundAsDescribed() = runMainTest {
        val viewModel = viewModel()
        viewModel.selectKind(SoundKind.EVENT)
        advanceUntilIdle()
        viewModel.moveStart(2_000)
        advanceUntilIdle()
        viewModel.selectCategory(SoundCategory.CITY)
        advanceUntilIdle()
        viewModel.changeName("Kettle")
        advanceUntilIdle()
        viewModel.togglePreview()
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        assertFalse(previewer.isPlaying.value)
        assertEquals(
            UserSoundDraft("Kettle", SoundCategory.CITY, SoundCategory.CITY.defaultIcon, SoundKind.EVENT, "/cache/import/audio", TrimRange(2_000, 10_000)),
            repository.drafts.single(),
        )
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun aChosenIconIsKept() = runMainTest {
        val viewModel = viewModel()
        viewModel.selectCategory(SoundCategory.CITY)
        advanceUntilIdle()
        viewModel.selectIcon(SoundIcon.TRAIN)
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        assertEquals(SoundIcon.TRAIN, repository.drafts.single().icon)
    }

    @Test
    fun aFailedSaveShowsAMessageAndCanBeRetried() = runMainTest {
        repository.createError = MediaException("codec")
        val viewModel = viewModel()
        viewModel.selectCategory(SoundCategory.CITY)
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isSaving)
        assertTrue(state.canSave)
        assertEquals(UiText.Resource(Res.string.mysounds_save_failed), state.userMessage)
    }
}
