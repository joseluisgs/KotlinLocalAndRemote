# Kotlin Local and Remote Service

Este proyecto, es un ejemplo de como crear un servicio local y remoto utilizando Kotlin.
Tendremos un repositorio local usando una base de datos, y un repositorio remoto utilizando una API REST.

Ambos repositorios estarán encapsulados en un solo servicio, que será el que utilicemos en nuestra aplicación.
Este servicio estará cacheado, es decir, si no hay conexión a internet, se obtendrán los datos de la base de datos
local.
Además, otra caché en memoria se encargará de almacenar los últimos datos obtenidos de la base de datos local o de la
API REST.

La base de datos local se irá actualizando con los datos obtenidos de la API REST, y la caché en memoria se irá
actualizando con los datos obtenidos de la base de datos local.
El intervalo de refresco de la base de datos local y el de la caché en memoria se puede configurar.

Por otro lado, el servicio podrá importar/exportar datos en CSV y JSON.

Finalmente tendremos un servicio de notificaciones, que nos permitirá recibir notificaciones de los cambios realizados.

El objetivo docente es mostrar implementaciones asíncronas en el procesamiento de la información.

Puedes seguir el proyecto en [GitHub]() y en los commits indicados.

