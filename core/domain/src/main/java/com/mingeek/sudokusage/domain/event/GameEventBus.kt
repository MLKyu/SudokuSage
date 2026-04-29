package com.mingeek.sudokusage.domain.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface GameEventBus {
    val events: SharedFlow<GameEvent>
    fun emit(event: GameEvent)
}

class DefaultGameEventBus : GameEventBus {
    private val _events = MutableSharedFlow<GameEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: SharedFlow<GameEvent> = _events.asSharedFlow()
    override fun emit(event: GameEvent) {
        _events.tryEmit(event)
    }
}
