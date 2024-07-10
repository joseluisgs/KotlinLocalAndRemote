package dev.joseluisgs.notification

import dev.joseluisgs.dto.TenistaDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging

private val logger = logging()

class TenistasNotifications : Notifications<TenistaDto> {
    // Implementamos el envío de notificaciones
    private val _notifications: MutableSharedFlow<Notification<TenistaDto>> =
        MutableSharedFlow(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        ) // MutableSharedFlow es un canal de comunicación
    val notifications: SharedFlow<Notification<TenistaDto>> =
        _notifications.asSharedFlow() // SharedFlow es un canal de solo lectura


    override suspend fun send(notification: Notification<TenistaDto>): Unit = withContext(Dispatchers.IO) {
        logger.debug { "Enviando notificación: $notification" }
        _notifications.tryEmit(notification)
    }
}