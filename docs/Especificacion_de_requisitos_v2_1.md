# Especificación de Requisitos — Versión 2.1

## Historial de versiones del documento

| Versión | Fecha      | Descripción          | Autor                                             |
|---------|------------|----------------------|---------------------------------------------------|
| 2.0     | 09/04/2026 | Inicio de documento  | Pardo, Munasipov, Tsatsorin, Chernishev           |

---

## Tabla de contenidos

1. [Objetivo](#1-objetivo)
2. [Beneficios](#2-beneficios)
3. [Alcance](#3-alcance)
4. [Limitaciones](#4-limitaciones)
5. [Requisitos no funcionales globales](#5-requisitos-no-funcionales-globales)
   - 5.1 [Módulo Venta de Tickets — Requisitos funcionales](#51-requisitos-funcionales)
   - 5.1.2 [Módulo Venta de Tickets — Requisitos no funcionales](#512-requisitos-no-funcionales)
   - 5.2 [Módulo Administración de Conciertos — Requisitos funcionales](#módulo-administración-de-conciertos)
6. [Prototipos de interfaz](#6-prototipos-de-interfaz)
7. [Glosario](#7-glosario)

---

## 1. Objetivo

El objetivo de este sistema es permitir la gestión y venta de tickets para conciertos de manera digital. El sistema permitirá administrar conciertos, vender entradas, controlar disponibilidad de sectores y validar tickets mediante códigos QR o códigos de barras durante el ingreso al evento.

Además, el sistema facilitará la compra de entradas por parte de los usuarios y permitirá a los administradores gestionar conciertos, sectores, disponibilidad de tickets y otros servicios adicionales como merchandising, estacionamiento y transporte.

---

## 2. Beneficios

Este sistema permitirá mejorar la organización y venta de entradas para conciertos, evitando errores manuales y sobreventas de tickets.

Los principales beneficios son:

- Centralizar la venta de tickets en un solo sistema.
- Controlar la disponibilidad de entradas en tiempo real.
- Reducir la reventa mediante límites de compra por usuario.
- Facilitar el acceso al evento mediante tickets digitales.
- Mejorar la gestión de conciertos por parte de los administradores.
- Permitir la venta adicional de merchandising.
- Administrar estacionamientos y transportes disponibles para el evento.

---

## 3. Alcance

El sistema será una aplicación de escritorio utilizada por administradores, organizadores y compradores.

### Administrador

El administrador podrá:

- Crear conciertos.
- Modificar conciertos.
- Definir sectores del concierto (VIP, platea, campo, etc.).
- Definir capacidad de cada sector.
- Bloquear o liberar tickets.
- Administrar merchandising.
- Administrar estacionamientos.
- Administrar transportes disponibles para los conciertos.
- Visualizar disponibilidad de tickets.

### Organizador

El organizador podrá:

- Crear conciertos.
- Modificar conciertos autorizados.
- Administrar información del evento.

### Comprador

El comprador podrá:

- Visualizar conciertos disponibles.
- Ver sectores y disponibilidad de tickets.
- Comprar tickets.
- Comprar hasta un máximo de 4 o 6 entradas por concierto.
- Recibir confirmación de compra por email o SMS.
- Recibir tickets digitales con código QR o código de barras.

### Sistema de acceso al concierto

El sistema permitirá validar tickets mediante el escaneo del QR o código de barras en el ingreso al evento.

---

## 4. Limitaciones

El sistema tendrá las siguientes limitaciones:

- El sistema funcionará únicamente como aplicación de escritorio.
- Los conciertos no podrán superponerse en misma fecha, hora y lugar.
- Cada sector tendrá un límite de capacidad definido por el administrador.
- Cada usuario podrá comprar un máximo de 4 a 6 tickets por concierto.
- La disponibilidad de tickets dependerá de la capacidad total del evento.
- La confiabilidad del sistema deberá ser mayor o igual a 0,99.

---

## 5. Requisitos no funcionales globales

- **RNFG1:** El sistema funcionará en sistema operativo Windows.
- **RNFG2:** El sistema deberá contar con conexión a internet para actualizar disponibilidad de tickets en tiempo real.
- **RNFG3:** El sistema deberá mantener una fiabilidad mínima de 0,99.
- **RNFG4:** El sistema deberá actualizar la disponibilidad de tickets en tiempo real para evitar ventas duplicadas.
- **RNFG5:** El sistema deberá soportar alta concurrencia de usuarios mediante fila virtual en caso de alto tráfico.

---

## Módulo Venta de Tickets

### 5.1 Requisitos funcionales

- **RFC1:** El comprador podrá visualizar conciertos disponibles a través de una lista en pantalla que muestre los eventos activos cargados en el sistema.
- **RFC2:** El comprador podrá visualizar información del concierto seleccionando un evento, mostrando artista, fecha, hora, lugar, capacidad total y sectores disponibles.
- **RFC3:** El comprador podrá visualizar la disponibilidad de tickets en tiempo real mediante la actualización automática de los cupos disponibles por sector.
- **RFC4:** El comprador podrá seleccionar el sector de la entrada eligiendo entre opciones disponibles como VIP, platea o campo antes de realizar la compra.
- **RFC5:** El comprador podrá comprar hasta 4 o 6 tickets por concierto mediante un control del sistema que limite la cantidad máxima por usuario en cada operación.
- **RFC6:** El comprador deberá ingresar nombre, apellido, correo electrónico, documento y datos de pago completando un formulario obligatorio antes de confirmar la compra.
- **RFC7:** El sistema generará un ticket digital con código QR o código de barras automáticamente una vez confirmada la compra.
- **RFC8:** El sistema enviará confirmación de compra por email o SMS utilizando los datos de contacto proporcionados por el comprador.
- **RFC9:** El personal de acceso podrá validar tickets escaneando el código QR o código de barras mediante un lector en la entrada del concierto.
- **RFC10:** El sistema permitirá implementar una fila virtual cuando se detecte alta concurrencia de usuarios, organizando el acceso de manera ordenada.
- **RFC11:** El comprador podrá comprar merchandising del evento agregando productos a su compra durante el proceso de adquisición de tickets.
- **RFC12:** El comprador podrá reservar estacionamiento para el evento seleccionando la opción correspondiente durante la compra del ticket.
- **RFC13:** El comprador podrá reservar transporte hacia el concierto eligiendo entre opciones disponibles definidas por el administrador, como camionetas con punto de salida específico (ej: Obelisco).

### 5.1.2 Requisitos no funcionales

- **RNFC1:** El sistema deberá mostrar disponibilidad de tickets en tiempo real.
- **RNFC2:** El tiempo de confirmación de compra no deberá superar los 5 segundos.
- **RNFC3:** El sistema deberá garantizar que no se vendan más tickets que la capacidad disponible.
- **RNFC4:** El sistema deberá permitir escaneo rápido de tickets en el acceso al evento.

---

## Módulo Administración de Conciertos

### Requisitos funcionales

- **RFA1:** El administrador podrá crear conciertos completando un formulario donde ingrese artista, fecha, hora, lugar, capacidad total y configuración inicial de sectores, validando que no exista otro concierto en el mismo lugar, fecha y hora.
- **RFA2:** El administrador podrá modificar conciertos editando los datos previamente cargados a través de un panel de gestión, siempre que el evento no haya comenzado o no afecte ventas ya realizadas.
- **RFA3:** El administrador podrá definir sectores del concierto creando, editando o eliminando sectores (VIP, platea, campo, etc.) desde una interfaz de configuración asociada a cada concierto.
- **RFA4:** El administrador podrá definir la capacidad de cada sector asignando una cantidad máxima de tickets disponibles por sector, asegurando que la suma no supere la capacidad total del evento.
- **RFA5:** El administrador podrá bloquear o liberar tickets seleccionando sectores o rangos de entradas desde el sistema, permitiendo restringir o habilitar su venta según necesidades operativas.
- **RFA6:** El administrador podrá visualizar la cantidad de tickets disponibles mediante paneles o reportes que muestren en tiempo real la cantidad total y por sector de tickets vendidos y disponibles.
- **RFA7:** El administrador podrá administrar merchandising del evento agregando, modificando o eliminando productos (ej: remeras, posters), definiendo precio, stock disponible y asociación con el concierto.
- **RFA8:** El administrador podrá administrar estacionamientos disponibles definiendo cantidad de espacios, ubicación, precio y disponibilidad, vinculados a cada concierto.
- **RFA9:** El administrador podrá administrar transportes disponibles para los conciertos creando opciones de transporte (ej: camionetas), indicando puntos de salida, horarios, capacidad y costos, asociados a cada evento.

---

## 6. Prototipos de interfaz

En esta etapa del proyecto no se desarrollaron prototipos de interfaz.

Las interfaces se definirán en una etapa posterior del desarrollo del sistema.

---

## 7. Glosario

| Término        | Definición                                                                                      |
|----------------|-------------------------------------------------------------------------------------------------|
| **Ticket**     | Entrada digital que permite el acceso al concierto.                                             |
| **Sector**     | Área específica del recinto del concierto (VIP, platea, campo, etc.).                           |
| **Administrador** | Usuario encargado de gestionar conciertos y configuraciones del sistema.                    |
| **QR**         | Código utilizado para validar la autenticidad del ticket en el ingreso al evento.               |
| **Fila virtual** | Sistema que organiza a los usuarios en espera cuando hay gran cantidad de accesos simultáneos. |
