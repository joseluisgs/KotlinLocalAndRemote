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
    // Usamos un MutableSharedFlow para manejar la cola de notificaciones y un SharedFlow para leerlas
    // El uso de un MutableSharedFlow permite que la cola de notificaciones sea modificable en tiempo real en otros hilos
    // El uso de un SharedFlow permite que los hilos que leen las notificaciones sean suscritos y reciban las notificaciones en tiempo real
    // Por que un SharedFlow permite que los hilos que leen las notificaciones sean suscritos y
    // reciban las notificaciones en tiempo real
    // Es una forma de comunicación asíncrona entre hilos
    private val _notifications: MutableSharedFlow<Notification<TenistaDto>> =
        MutableSharedFlow(
            replay = 1, // Solo se guarda la última notificación
            onBufferOverflow = BufferOverflow.DROP_OLDEST // Si se llega a la capacidad máxima, se elimina la notific
        ) // MutableSharedFlow es un canal de comunicación
    val notifications: SharedFlow<Notification<TenistaDto>> =
        _notifications.asSharedFlow() // SharedFlow es un canal de solo lectura


    override suspend fun send(notification: Notification<TenistaDto>): Unit = withContext(Dispatchers.IO) {
        logger.debug { "Enviando notificación: $notification" }
        _notifications.tryEmit(notification)
    }
}