package com.mustafakoceerr.justrelax.feature.mysounds

import com.mustafakoceerr.justrelax.core.media.ImportedAudio
import com.mustafakoceerr.justrelax.core.media.MediaException
import com.mustafakoceerr.justrelax.core.model.SoundIcon
import com.mustafakoceerr.justrelax.core.model.SoundSource
import com.mustafakoceerr.justrelax.core.model.soundscape.SoundKind
import com.mustafakoceerr.justrelax.core.testing.fake.FakeAudioImporter
import com.mustafakoceerr.justrelax.core.testing.fake.FakeUserSoundRepository
import com.mustafakoceerr.justrelax.core.testing.observe
import com.mustafakoceerr.justrelax.core.testing.runMainTest
import com.mustafakoceerr.justrelax.core.testing.testClip
import com.mustafakoceerr.justrelax.core.testing.testSound
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class MySoundsViewModelTest {

    private val kettle = testSound(
        "user-1", kind = SoundKind.EVENT, source = SoundSource.USER,
        clips = listOf(testClip(remoteUrl = null, localPath = "/user/user-1.m4a")),
    ).copy(names = mapOf("en" to "Kettle"), icon = SoundIcon.CAFE)

    private val repository = FakeUserSoundRepository(listOf(kettle))
    private val importer = FakeAudioImporter()

    private fun TestScope.viewModel() = MySoundsViewModel(repository, importer).also { observe(it.uiState) }

    @Test
    fun theUsersSoundsAreListedWithTheirNamesKindsAndLengths() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf(MySoundItem("user-1", "Kettle", SoundKind.EVENT, SoundIcon.CAFE, 2_000)), state.sounds)
    }

    @Test
    fun aPickedFileIsCopiedAndOpensTheEditor() = runMainTest {
        importer.result = ImportedAudio("/cache/import/audio", "Kettle boiling.m4a")
        val viewModel = viewModel()

        viewModel.import("content://picked")
        advanceUntilIdle()

        assertEquals(ImportedAudio("/cache/import/audio", "Kettle boiling.m4a"), viewModel.uiState.value.editorToOpen)
        assertFalse(viewModel.uiState.value.isImporting)

        viewModel.onEditorOpened()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun aCancelledPickerDoesNothing() = runMainTest {
        val viewModel = viewModel()

        viewModel.import(null)
        advanceUntilIdle()

        assertEquals(emptyList(), importer.importedUris)
        assertNull(viewModel.uiState.value.editorToOpen)
    }

    @Test
    fun aFileThatCannotBeCopiedShowsAMessage() = runMainTest {
        importer.error = MediaException("unreadable")
        val viewModel = viewModel()

        viewModel.import("content://picked")
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.editorToOpen)
        assertEquals(MySoundsMessage.ImportFailed, viewModel.uiState.value.userMessage)
    }

    @Test
    fun deletingAsksFirstThenRemovesTheSound() = runMainTest {
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.requestDelete("user-1")
        advanceUntilIdle()
        assertEquals("user-1", viewModel.uiState.value.soundToDelete?.id)

        viewModel.dismissDelete()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.soundToDelete)
        assertEquals(1, viewModel.uiState.value.sounds.size)

        viewModel.requestDelete("user-1")
        advanceUntilIdle()
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(emptyList(), viewModel.uiState.value.sounds)
        assertNull(viewModel.uiState.value.soundToDelete)
    }
}
