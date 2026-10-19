package com.mustafakoceerr.justrelax.di

import androidx.lifecycle.ViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ViewModelHostTest {

    private class NamedViewModel(val name: String) : ViewModel() {
        var isCleared = false
            private set

        override fun onCleared() {
            isCleared = true
        }
    }

    private val koin = koinApplication {
        modules(module { viewModel { (name: String) -> NamedViewModel(name) } })
    }.koin

    @Test
    fun aViewModel_comesFromKoin_withTheGivenParameters() {
        val host = ViewModelHost(koin)

        val viewModel = host.get(NamedViewModel::class, listOf("rain"))

        assertEquals("rain", viewModel.name)
    }

    @Test
    fun aHost_keepsItsViewModel() {
        val host = ViewModelHost(koin)

        assertSame(host.get(NamedViewModel::class, listOf("rain")), host.get(NamedViewModel::class, listOf("rain")))
    }

    @Test
    fun eachHost_hasItsOwnViewModel() {
        assertNotSame(
            ViewModelHost(koin).get(NamedViewModel::class, listOf("rain")),
            ViewModelHost(koin).get(NamedViewModel::class, listOf("rain")),
        )
    }

    @Test
    fun clearing_endsTheViewModel() {
        val host = ViewModelHost(koin)
        val viewModel = host.get(NamedViewModel::class, listOf("rain"))
        assertFalse(viewModel.isCleared)

        host.clear()

        assertTrue(viewModel.isCleared)
    }
}
