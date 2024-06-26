plugins {
    kotlin("jvm") version "1.9.23"
    // Plugin de serialización
    kotlin("plugin.serialization") version "1.9.23"
    // SqlDelight
    id("app.cash.sqldelight") version "2.0.2"
}

group = "dev.joseluisgs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Logger
    implementation("org.lighthousegames:logging:1.3.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    // Serialización JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    // Result ROP
    implementation("com.michael-bull.kotlin-result:kotlin-result:2.0.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // SqlDelight Database
    implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
    // SqlDelight Coroutines Extensions
    // implementation("app.cash.sqldelight:coroutines-extensions:2.0.2")

    // Para test
    testImplementation(kotlin("test"))
    // Para testear coroutines
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    // Para Mockear con Movkk
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

// Hacer un Jar ejecutable
tasks.jar {
    manifest {
        // Clase principal
        attributes["Main-Class"] = "dev.joseluisgs.MainKt"
    }
    // Incluir dependencias
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    // Excluir duplicados
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Configuración del plugin de SqlDeLight
sqldelight {
    databases {
        // Nombre de la base de datos
        create("AppDatabase") {
            // Paquete donde se generan las clases
            packageName.set("dev.joseluisgs.database")
        }
    }
}
