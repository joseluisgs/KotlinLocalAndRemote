package di

import dev.joseluisgs.di.appModule
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.fileProperties
import org.koin.test.KoinTest
import org.koin.test.verify.verify
import kotlin.test.Test

@OptIn(KoinExperimentalAPI::class)
class ModuleVerificationTest : KoinTest {

    @Test
    fun verifyModules() {
        koinApplication {
            fileProperties("/config.properties")
            appModule.verify(extraTypes = listOf(Boolean::class, app.cash.sqldelight.db.SqlDriver::class))
        }
    }
}