package notifications

import dev.joseluisgs.models.Tenista
import dev.joseluisgs.notifications.Notification
import dev.joseluisgs.notifications.TenistasNotifications
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test


class TenistasNotificationsTest {
    @Test
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
        )
        val notification =
            Notification(Notification.Type.CREATE, testTenista, "Creación de tenista", LocalDateTime.now())
        val tenistasNotifications = TenistasNotifications()

        // Act
        tenistasNotifications.send(notification)

        // Assert
        val emittedNotification = tenistasNotifications.notifications.distinctUntilChanged().first()

        assertAll(
            { assertEquals(notification, emittedNotification) },
            { assertEquals(notification.type, emittedNotification.type) },
            { assertEquals(notification.item, emittedNotification.item) },
            { assertEquals(notification.createdAt, emittedNotification.createdAt) }
        )
    }
}