# API del Sistema de Gestión de Bibliotecas

**Aprendiz** · Santiago Baratto  
**Programa** · Análisis y Desarrollo de Software — SENA  
**Evidencia** · GA7-220501096-AA5-EV03

La API permite gestionar el catálogo de libros, consultar categorías, registrar
lectores y controlar préstamos y devoluciones. El proyecto usa Java, Tomcat y
MariaDB. Para ejecutarlo, inicia MySQL en XAMPP y ejecuta SistemaBibliotecaWeb
desde NetBeans.

**URL base local** · `http://localhost:8080/SistemaBibliotecaWeb`  
**Formato de entrada** · Para las solicitudes con cuerpo usa `Content-Type: application/json`.

| Método y ruta | Entrada | Respuesta esperada |
|---|---|---|
| `GET /api/libros` | Sin cuerpo | `200` con la lista de libros. Si está vacía, devuelve `[]`. |
| `GET /api/libros/{id}` | ID del libro en la URL | `200` con el libro, `400` si el ID es inválido o `404` si no existe. |
| `POST /api/libros` | JSON de libro | `201` con mensaje de registro o `400` si los datos son inválidos. |
| `PUT /api/libros/{id}` | ID del libro y JSON con los datos completos | `200` con mensaje de actualización, `400` si la entrada es inválida o `404` si el libro no existe. |
| `DELETE /api/libros/{id}` | ID del libro, sin cuerpo | `204` con cuerpo vacío, `400` si el ID es inválido o `404` si el libro no existe. |
| `GET /api/categorias` | Sin cuerpo | `200` con la lista de categorías. Cada categoría incluye `idCategoria`, `nombre` y `literatura`. |
| `GET /api/usuarios` | Sin cuerpo | `200` con la lista de lectores. |
| `POST /api/usuarios` | JSON de lector | `201` con mensaje e `idUsuario`, o `400` si los datos son inválidos. |
| `GET /api/prestamos` | Sin cuerpo | `200` con la lista de préstamos y sus fechas. |
| `POST /api/prestamos` | JSON con `idUsuario` e `idLibro` | `201` con `idPrestamo` y descuento de una unidad del stock, `400` por entrada inválida, `404` si el usuario no existe o `409` si el libro no existe o no tiene stock. |
| `PUT /api/prestamos/{id}/devolucion` | ID del préstamo, sin cuerpo | `200` con mensaje de devolución y reposición de una unidad, `400` para un ID numérico fuera del rango válido, `404` para ruta o préstamo inexistente, o `409` si ya se devolvió o el libro asociado no está disponible en la base de datos. |

Las operaciones también pueden devolver `500` ante fallos de consulta o escritura.
El listado de libros usa el DAO original, que puede devolver `[]` si falla la consulta.
En los préstamos, `fechaDevolucion` representa la fecha real en que se devuelve el libro.

## Ejemplo de JSON para registrar o actualizar un libro

```json
{"titulo":"Cien años de soledad","autor":"Gabriel García Márquez","stock":5,"idCategoria":1}
```

Usa un `idCategoria` existente. En `PUT`, el campo `stock` es obligatorio.

## Ejemplo de JSON para registrar un lector

```json
{"nombre":"Lector de prueba","correo":"lector@example.com","telefono":"3000000000","tipoUsuario":"Estudiante"}
```

El nombre es obligatorio. Los demás campos son opcionales.

## Ejemplo de JSON para registrar un préstamo

```json
{"idUsuario":1,"idLibro":2}
```

Reemplaza los IDs del ejemplo por los de un lector registrado y un libro con stock.
Para devolverlo, usa el `idPrestamo` devuelto al registrarlo.