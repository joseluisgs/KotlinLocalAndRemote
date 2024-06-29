plugins {
    kotlin("jvm") version "1.9.23"
    // Plugin de serialización
    kotlin("plugin.serialization") version "1.9.23"
    // SqlDelight
    id("app.cash.sqldelight") version "2.0.2"
    // KSP de Google
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    // Ktorfit
    id("de.jensklingenberg.ktorfit") version "1.13.0"
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
    // Ktorfit
    implementation("de.jensklingenberg.ktorfit:ktorfit-lib:1.13.0")
    ksp("de.jensklingenberg.ktorfit:ktorfit-ksp:1.13.0")
    // Ktor client opciones
    // Para serializar en Json con Ktor
    implementation("io.ktor:ktor-client-serialization:2.3.10")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
    // Koin DI
    // Koin, con BOM ya se instalan todas las dependencias necesarias con la versión correcta
    implementation(platform("io.insert-koin:koin-bom:3.5.6"))
    implementation("io.insert-koin:koin-core") // Core
    implementation("io.insert-koin:koin-test") // Para test y usar checkModules

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
