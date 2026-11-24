# Gestión móvil del registro y validación de ventas

Caso **DSY1105_004D** · Desarrollo de Aplicaciones Móviles · Duoc UC

Aplicación Android en Kotlin + Jetpack Compose con arquitectura MVVM. Registra ventas con datos ficticios, las guarda en Room, las sincroniza con un microservicio propio en Spring Boot y consume una API externa con Retrofit.

---

## 1. Nombre del proyecto

**Gestión móvil del registro y validación de ventas** (`GestionVentas`)

## 2. Integrantes

| Integrante | Usuario GitHub |
|---|---|
| Javier Valtierra | `mafuyuu00` |
| _Nombre integrante 2_ | _usuario_ |

## 3. Funcionalidades

- **Login → Menú → Lista de ventas → Formulario de venta**, con navegación a través de `AppNavigation`.
- **ListaVentasScreen**: muestra las ventas guardadas, con búsqueda y filtro por estado.
  - Las ventas en Borrador se pueden editar o eliminar.
  - Las Recibidas solo se pueden consultar.
  - Cada venta indica si está sincronizada con el microservicio.
- **VentaFormScreen**: registra y modifica ventas. Valida en el ViewModel y muestra los errores en cada campo:
  - Convenio, fecha y cliente obligatorios.
  - Monto numérico y mayor que 0.
  - Código de autorización obligatorio y no duplicado.
- **Recurso nativo (cámara)**:
  - Foto del respaldo, vinculada a la venta.
  - Lector QR para el código de autorización.
- **Persistencia local** con Room.
- **Persistencia remota** en el microservicio `ms-ventas`: crear, modificar, eliminar y sincronizar.
- **API externa** JSONPlaceholder: en el Panel Administrador, el botón *Consultar API externa* muestra el listado de posts.
- **Panel Administrador**: QR y comprobante PDF de las ventas recibidas.

## 4. Endpoints utilizados

### Microservicio propio `ms-ventas` (Spring Boot + H2) — `http://localhost:8080`
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/ventas` | Lista las ventas |
| GET | `/api/ventas/{id}` | Obtiene una venta |
| POST | `/api/ventas` | Registra una venta (409 si el código ya existe) |
| PUT | `/api/ventas/{id}` | Modifica una venta |
| DELETE | `/api/ventas/{id}` | Elimina una venta en Borrador |

### API externa — JSONPlaceholder
| Método | Endpoint | Uso |
|---|---|---|
| GET | `https://jsonplaceholder.typicode.com/posts` | Listado de posts mostrado en el Panel Administrador |

> La app llama al microservicio en `http://localhost:8080/` (`RetrofitClient.kt`). Para que el celular o el emulador lleguen al puerto 8080 del PC, se usa `adb reverse tcp:8080 tcp:8080` (paso 2 de la ejecución). Así funciona en cualquier red Wi-Fi, sin cambiar IP ni firewall.

## 5. Pasos para ejecutar

1. Levantar el microservicio:
   ```powershell
   cd microservicios\ms-ventas
   .\mvnw.cmd spring-boot:run
   ```
   - Si no encuentra Java: `$env:JAVA_HOME = "<ruta de Android Studio>\jbr"`.
   - Para comprobar que funciona: http://localhost:8080/api/ventas
   - Consola H2: http://localhost:8080/h2-console, con JDBC URL `jdbc:h2:file:./data/ventasdb` y usuario `sa`.
2. Con el celular (USB o depuración inalámbrica) o el emulador conectado, abrir la terminal de Android Studio y ejecutar:
   ```powershell
   adb reverse tcp:8080 tcp:8080
   ```
   El túnel se debe repetir cada vez que el dispositivo se vuelve a conectar.
3. Abrir el proyecto en Android Studio. El Gradle JDK queda en 21. Hacer *Sync* y ejecutar la app.
4. Pruebas:
   - Unitarias (Kotest + MockK) en `app/src/test`: clic derecho → *Run 'Tests in ...'*.
   - De interfaz (Compose UI Test) en `app/src/androidTest`.

## 6. APK firmado y llave `.jks`

- APK firmado: [`apk/GestionVentas-release.apk`](apk/GestionVentas-release.apk)
- Llave: [`app/release-key.jks`](app/release-key.jks), con alias `gestionventas_alias`.
- Configuración: `signingConfigs` en [`app/build.gradle.kts`](app/build.gradle.kts).

_Captura del APK firmado y del .jks:_ _(agregar imagen)_

### Planificación (Trello)
_Agregar enlace o captura del tablero._
