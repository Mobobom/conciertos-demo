# Sistema de Gestión y Venta de Tickets de Conciertos

Aplicación de escritorio Java/Swing que gestiona y vende entradas para conciertos. Roles previstos: **Administrador**, **Organizador**, **Comprador** y **Personal de acceso**.

Basado en la especificación de requisitos `Especificacion_de_requisitos_-_Version_2_1.pdf`, sigue el estilo de código de `Style_example` (capas BLL/DLL/GUI/repository).

## Estructura del proyecto

```
rentacar-demo-main/
├── db/
│   ├── setup_mysql_ticketing.sh    Crea usuario y base de datos
│   ├── create_ticketing.sql        Crea las tablas del esquema
│   └── populate_ticketing.sql      Carga datos de prueba
├── lib/
├── src/
│   ├── BLL/    Entidades
│   ├── DLL/    Conexión JDBC y controladores
│   ├── GUI/    Pantallas
│   └── repository/
└── README.md
```

## Requisitos

- **Java**
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

# Ejecutar
DISPLAY=:0 /usr/lib/jvm/java-11-openjdk-amd64/bin/java -cp "bin:lib/*" GUI.Main
```

Al iniciar, en consola se verá:

```
Conexion: conectado a jdbc:mysql://localhost:3306/ticketing?useSSL=false&serverTimezone=UTC
```

Y se abrirá una ventana **"Conciertos disponibles"** con dos filas: *Coldplay* (15/06/2026, Estadio Monumental) y *Taylor Swift* (20/08/2026, Estadio Velez), ambas con 60 entradas disponibles.

## Datos de prueba

### Usuarios

| Email | Contraseña | Rol |
|---|---|---|
| admin@ticket.com | admin123 | Administrador |
| org@ticket.com | org123 | Organizador |
| acceso@ticket.com | acceso123 | Personal de acceso |
| juan@mail.com | 1234 | Comprador |
| ana@mail.com | 1234 | Comprador |

Los hashes bcrypt cargados son **placeholders** bien formados que **no** validan contra las contraseñas listadas. Se regenerarán con `Hashing.hash(...)` cuando se implemente el flujo de login en la próxima ronda.

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

Ver `db/create_ticketing.sql` para el detalle completo, y docs/superpowers/specs/2026-05-26-ticketing-system-design.md` para el diseño general del sistema (incluyendo las funcionalidades diferidas).

## Notas técnicas

- **Driver JDBC**: se usa `mysql-connector-j-8.4.0.jar`, porque  los conectores antiguos no soportan el plugin de autenticación `caching_sha2_password` que MySQL 8 utiliza por defecto.
- **URL JDBC**: `jdbc:mysql://localhost:3306/ticketing?useSSL=false&serverTimezone=UTC` (definida en `src/DLL/Conexion.java`).
