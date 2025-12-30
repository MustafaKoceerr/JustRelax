//package com.mustafakoceerr.justrelax.core.audio.timer
//
//import com.mustafakoceerr.justrelax.core.common.dispatcher.DispatcherProvider
//import com.mustafakoceerr.justrelax.core.domain.player.AudioMixer
//import com.mustafakoceerr.justrelax.core.domain.timer.TimerManager
//import com.mustafakoceerr.justrelax.core.domain.timer.TimerState
//import com.mustafakoceerr.justrelax.core.domain.timer.TimerStatus
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.map
//import kotlinx.coroutines.flow.update
//import kotlinx.coroutines.launch
//
//class TimerManagerImpl(
//    private val audioMixer: AudioMixer,
//    private val dispatchers: DispatcherProvider,
//    private val appScope: CoroutineScope
//) : TimerManager {
//
//    private val _state = MutableStateFlow(TimerState())
//    override val state: StateFlow<TimerState> = _state.asStateFlow()
//
//    override val isRunning: kotlinx.coroutines.flow.Flow<Boolean> = _state.map { it.status == TimerStatus.RUNNING }
//
//    private var timerJob: Job? = null
//
//    // INIT BLOĞU SİLİNDİ.
//    // Artık Mixer'ı dinleyip kendini iptal etmiyor.
//    // Kullanıcı sesi kapatsa bile Timer arkada (RAM'de) saymaya devam eder.
//
//    override fun startTimer(seconds: Long) {
//        timerJob?.cancel()
//
//        _state.update {
//            TimerState(
//                status = TimerStatus.RUNNING,
//                totalSeconds = seconds,
//                remainingSeconds = seconds
//            )
//        }
//
//        timerJob = appScope.launch(dispatchers.default) {
//            while (_state.value.remainingSeconds > 0) {
//                delay(1000L)
//
//                if (_state.value.status == TimerStatus.PAUSED) continue
//
//                _state.update {
//                    it.copy(remainingSeconds = it.remainingSeconds - 1)
//                }
//            }
//            onTimerFinished()
//        }
//    }
//
//    override fun pauseTimer() {
//        if (_state.value.status == TimerStatus.RUNNING) {
//            _state.update { it.copy(status = TimerStatus.PAUSED) }
//        }
//    }
//
//    override fun resumeTimer() {
//        if (_state.value.status == TimerStatus.PAUSED) {
//            _state.update { it.copy(status = TimerStatus.RUNNING) }
//        }
//    }
//
//    override fun cancelTimer() {
//        timerJob?.cancel()
//        timerJob = null
//        _state.update { TimerState(status = TimerStatus.IDLE) }
//    }
//
//    private fun onTimerFinished() {
//        // Süre doldu!
//        // O an ses çalıyorsa durur, çalmıyorsa hiçbir şey olmaz.
//        audioMixer.stopAll()
//        cancelTimer()
//    }
//}