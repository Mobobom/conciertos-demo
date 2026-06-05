# Sistema de Gestión y Venta de Tickets de Conciertos

Aplicación de escritorio Java/Swing que gestiona y vende entradas para conciertos. Roles previstos: **Administrador**, **Organizador**, **Comprador** y **Personal de acceso**.

Basado en la especificación de requisitos `Especificacion_de_requisitos_-_Version_2_1.pdf`, sigue el estilo de código de `Style_example` (capas BLL/DLL/GUI).

## Estructura del proyecto

```
conciertos-demo-main/
├── db/
│   ├── setup_mysql_ticketing.sh    Crea usuario y base de datos
│   ├── create_ticketing.sql        Crea las tablas del esquema
│   └── populate_ticketing.sql      Carga datos de prueba
├── lib/
├── src/
│   ├── BLL/    Entidades y servicios (lógica de negocio)
│   ├── DLL/    Conexión JDBC y controladores (acceso a datos)
│   └── GUI/    Pantallas Swing (login, menús por rol, tablas)
└── README.md
```

## Requisitos

- **Java** 11 o superior
- **MySQL** 8.x

## Configuración de la base de datos

### 1. Crear el usuario y la base de datos (una sola vez, con sudo)

```bash
sudo bash db/setup_mysql_ticketing.sh
```

Esto crea:

- Base de datos `ticketing`
- Usuario `ticketing@localhost` con contraseña `Db_ticketing_2026!`
- Privilegios completos sobre la base `ticketing`

### 2. Crear las tablas

```bash
mysql -u ticketing -p'Db_ticketing_2026!' ticketing < db/create_ticketing.sql
```

El script es idempotente: elimina las tablas existentes antes de recrearlas, por lo que puede ejecutarse las veces que sea necesario durante el desarrollo.

### 3. Cargar datos de prueba

```bash
mysql -u ticketing -p'Db_ticketing_2026!' ticketing < db/populate_ticketing.sql
```

Verificación rápida:

```bash
mysql -u ticketing -p'Db_ticketing_2026!' ticketing -e \
  "SELECT (SELECT COUNT(*) FROM usuario)   AS usuarios,
          (SELECT COUNT(*) FROM concierto) AS conciertos,
          (SELECT COUNT(*) FROM sector)    AS sectores,
          (SELECT COUNT(*) FROM ticket)    AS tickets;"
```

Resultado esperado: `5 usuarios, 2 conciertos, 6 sectores, 120 tickets`.

## Compilar y ejecutar

```bash
# Compilar (apuntando a JRE 11 por compatibilidad con el JRE instalado)
mkdir -p bin
javac --release 11 -d bin -cp "lib/*" $(find src -name "*.java")

# Ejecutar en macOS / Linux
java -cp "bin:lib/*" GUI.Main
```

> El conector JDBC (`lib/mysql-connector-j-8.4.0.jar`) debe estar en el classpath
> tanto al compilar como al ejecutar; por eso ambos comandos incluyen `lib/*`.

Al iniciar, se abrirá la pantalla de **Login**. Se puede probar con:

```text
admin@ticket.com / admin123
juan@mail.com / 1234
```

En consola se verá:

```
Conexion: conectado a jdbc:mysql://localhost:3306/ticketing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

La ventana **"Conciertos disponibles"** muestra las columnas *ID, Artista, Fecha, Hora, Lugar, Capacidad, Disponibles* con dos filas de prueba: *Coldplay* (15/06/2026, Estadio Monumental) y *Taylor Swift* (20/08/2026, Estadio Velez), ambas con 60 entradas disponibles. Bajo la tabla hay una barra de botones (**Subir / Bajar / Actualizar / Cerrar**, y **Comprar seleccionado** cuando se abre desde el menú del comprador).

## Flujo de la aplicación y roles

Tras autenticarse se abre una pantalla principal según el rol:

- **Administrador** → `MenuAdministrador`: alta, modificación y cancelación de conciertos; gestión de sectores y generación de tickets; bloqueo/liberación de tickets.
- **Organizador** → `MenuOrganizador`: creación y modificación de conciertos y consulta de la información del evento.
- **Comprador** → `MenuComprador`: ver conciertos, comprar tickets (sector → cantidad → método de pago) y consultar sus tickets comprados.
- **Personal de acceso** → `MenuPersonalAcceso`: validación de tickets por código.

Administrador y Organizador acceden directamente a su menú de gestión; Comprador y Personal de acceso ven primero una pantalla base (`RoleHomeFrame`) desde la que abren su menú de rol, la tabla de conciertos o (comprador) sus tickets comprados.

Las tablas de conciertos, sectores y tickets incluyen una barra de botones para operar sobre la **fila seleccionada** (editar, eliminar, cancelar, bloquear/liberar, según la pantalla), además de **Subir/Bajar** para reordenar la vista, **Actualizar** para recargar desde la base de datos y **Cerrar**. El reordenamiento es solo visual y no se persiste.

## Datos de prueba

### Usuarios

| Email | Contraseña | Rol |
|---|---|---|
| admin@ticket.com | admin123 | Administrador |
| org@ticket.com | org123 | Organizador |
| acceso@ticket.com | acceso123 | Personal de acceso |
| juan@mail.com | 1234 | Comprador |
| ana@mail.com | 1234 | Comprador |

Los hashes bcrypt cargados validan contra las contraseñas listadas y son usados por `UsuarioService` para el flujo de autenticación del MVP.

### Conciertos

| Artista | Fecha | Hora | Lugar | Capacidad |
|---|---|---|---|---|
| Coldplay | 2026-06-15 | 21:00 | Estadio Monumental | 60 |
| Taylor Swift | 2026-08-20 | 20:30 | Estadio Velez | 60 |

Cada concierto tiene tres sectores (VIP=10, Platea=20, Campo=30) con tickets
pre-generados en estado `Disponible`.

## Esquema de base de datos

12 tablas, todas InnoDB / utf8mb4, con FK e índices:

- `usuario` (rol: Administrador/Organizador/Comprador/PersonalAcceso)
- `concierto` (`UNIQUE(fecha, hora, lugar)` evita superposiciones)
- `sector` (VIP, Platea, Campo, Preferencial)
- `ticket` (con `codigo` UNIQUE para validación QR)
- `compra` y `pago`
- `merchandising` y `compra_merchandising`
- `estacionamiento` y `reserva_estacionamiento`
- `transporte` y `reserva_transporte`

Ver `db/create_ticketing.sql` para el detalle completo, y `docs/Especificacion_de_requisitos_v2_1.md` para la especificación de requisitos del sistema (incluyendo las funcionalidades diferidas).

## Notas técnicas

- **Driver JDBC**: se usa `mysql-connector-j-8.4.0.jar`, porque los conectores antiguos no soportan el plugin de autenticación `caching_sha2_password` que MySQL 8 utiliza por defecto.
- **URL JDBC**: `jdbc:mysql://localhost:3306/ticketing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` (definida en `src/DLL/Conexion.java`).
- **Autenticación MySQL 8**: el parámetro `allowPublicKeyRetrieval=true` permite el login con `caching_sha2_password` sobre una conexión sin SSL (entorno de desarrollo local).
- **Contraseñas**: se almacenan como hashes bcrypt (`lib/jbcrypt-0.4.jar`); `UsuarioService` valida con `BCrypt.checkpw`.
