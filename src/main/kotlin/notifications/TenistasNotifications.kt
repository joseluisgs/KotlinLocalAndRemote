package dev.joseluisgs.notifications

import dev.joseluisgs.models.Tenista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.lighthousegames.logging.logging

private val logger = logging()

class TenistasNotifications : Notifications<Tenista> {
    // Implementamos el envío de notificaciones
    private val _notifications: MutableSharedFlow<Notification<Tenista>> =
        MutableSharedFlow(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        ) // MutableSharedFlow es un canal de comunicación
    val notifications: SharedFlow<Notification<Tenista>> =
        _notifications.asSharedFlow() // SharedFlow es un canal de solo lectura


    override suspend fun send(notification: Notification<Tenista>): Unit = withContext(Dispatchers.IO) {
        logger.debug { "Enviando notificación: $notification" }
        _notifications.tryEmit(notification)
    }
}