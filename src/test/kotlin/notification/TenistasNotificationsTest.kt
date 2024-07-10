package notification

import dev.joseluisgs.mapper.toTenistaDto
import dev.joseluisgs.model.Tenista
import dev.joseluisgs.notification.Notification
import dev.joseluisgs.notification.TenistasNotifications
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test


class TenistasNotificationsTest {
    @Test
    @DisplayName("Notificación es emitida")
    fun `notificacion es emitida`() = runTest {
        // Arrange
        val testTenista = Tenista(
            id = 1,
            nombre = "Rafael Nadal",
            pais = "España",
            altura = 185,
            peso = 85,
            puntos = 10000,
            mano = Tenista.Mano.ZURDO,
            fechaNacimiento = LocalDate.of(1986, 6, 3),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isDeleted = false
        ).toTenistaDto()
        val notification =
            Notification(Notification.Type.CREATE, testTenista, "Creación de tenista", LocalDateTime.now())
        val tenistasNotifications = TenistasNotifications()

        // Act
        tenistasNotifications.send(notification)

        // Assert
        val emittedNotification = tenistasNotifications.notifications.distinctUntilChanged().first()

        assertAll(
            { assertEquals(notification, emittedNotification, "La notificación debe ser la misma") },
            { assertEquals(notification.type, emittedNotification.type, "El tipo de notificación debe ser el mismo") },
            {
                assertEquals(
                    notification.item,
                    emittedNotification.item,
                    "El ítem de la notificación debe ser el mismo"
                )
            },
            {
                assertEquals(
                    notification.createdAt,
                    emittedNotification.createdAt,
                    "La fecha de creación debe ser la misma"
                )
            }
        )
    }
}