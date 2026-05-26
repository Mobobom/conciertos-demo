-- Concert Ticketing System — schema
-- Run after db/setup_mysql_ticketing.sh (which creates the database and the `ticketing` user).
-- Invoke:  mysql -u ticketing -p ticketing < db/create_ticketing.sql

USE `ticketing`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `reserva_transporte`;
DROP TABLE IF EXISTS `transporte`;
DROP TABLE IF EXISTS `reserva_estacionamiento`;
DROP TABLE IF EXISTS `estacionamiento`;
DROP TABLE IF EXISTS `compra_merchandising`;
DROP TABLE IF EXISTS `merchandising`;
DROP TABLE IF EXISTS `pago`;
DROP TABLE IF EXISTS `ticket`;
DROP TABLE IF EXISTS `compra`;
DROP TABLE IF EXISTS `sector`;
DROP TABLE IF EXISTS `concierto`;
DROP TABLE IF EXISTS `usuario`;
SET FOREIGN_KEY_CHECKS = 1;

-- usuario

CREATE TABLE `usuario` (
  `id`        INT(11)      NOT NULL AUTO_INCREMENT,
  `nombre`    VARCHAR(60)  NOT NULL,
  `apellido`  VARCHAR(60)  NOT NULL,
  `email`     VARCHAR(100) NOT NULL,
  `documento` VARCHAR(20)  DEFAULT NULL,
  `password`  VARCHAR(100) NOT NULL,
  `rol`       ENUM('Administrador','Organizador','Comprador','PersonalAcceso') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_usuario_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- concierto

CREATE TABLE `concierto` (
  `id`              INT(11)      NOT NULL AUTO_INCREMENT,
  `artista`         VARCHAR(100) NOT NULL,
  `fecha`           DATE         NOT NULL,
  `hora`            TIME         NOT NULL,
  `lugar`           VARCHAR(100) NOT NULL,
  `capacidad_total` INT(11)      NOT NULL,
  `organizador_id`  INT(11)      DEFAULT NULL,
  `estado`          ENUM('Activo','Cancelado') NOT NULL DEFAULT 'Activo',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_concierto_fecha_hora_lugar` (`fecha`,`hora`,`lugar`),
  KEY `fk_concierto_organizador` (`organizador_id`),
  CONSTRAINT `fk_concierto_organizador`
    FOREIGN KEY (`organizador_id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- sector

CREATE TABLE `sector` (
  `id`            INT(11)      NOT NULL AUTO_INCREMENT,
  `concierto_id`  INT(11)      NOT NULL,
  `tipo`          ENUM('VIP','Platea','Campo','Preferencial') NOT NULL,
  `nombre`        VARCHAR(50)  NOT NULL,
  `capacidad`     INT(11)      NOT NULL,
  `precio`        DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_sector_concierto` (`concierto_id`),
  CONSTRAINT `fk_sector_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- compra 

CREATE TABLE `compra` (
  `id`            INT(11)       NOT NULL AUTO_INCREMENT,
  `comprador_id`  INT(11)       NOT NULL,
  `concierto_id`  INT(11)       NOT NULL,
  `fecha`         DATETIME      NOT NULL,
  `total`         DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_compra_comprador` (`comprador_id`),
  KEY `fk_compra_concierto` (`concierto_id`),
  CONSTRAINT `fk_compra_comprador`
    FOREIGN KEY (`comprador_id`) REFERENCES `usuario` (`id`),
  CONSTRAINT `fk_compra_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ticket

CREATE TABLE `ticket` (
  `id`            INT(11)      NOT NULL AUTO_INCREMENT,
  `concierto_id`  INT(11)      NOT NULL,
  `sector_id`     INT(11)      NOT NULL,
  `codigo`        VARCHAR(50)  NOT NULL,
  `precio`        DECIMAL(10,2) NOT NULL,
  `estado`        ENUM('Disponible','Vendido','Bloqueado','Usado') NOT NULL DEFAULT 'Disponible',
  `compra_id`     INT(11)      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_ticket_codigo` (`codigo`),
  KEY `fk_ticket_concierto` (`concierto_id`),
  KEY `fk_ticket_sector` (`sector_id`),
  KEY `fk_ticket_compra` (`compra_id`),
  KEY `ix_ticket_estado` (`estado`),
  CONSTRAINT `fk_ticket_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ticket_sector`
    FOREIGN KEY (`sector_id`) REFERENCES `sector` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ticket_compra`
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- pago

CREATE TABLE `pago` (
  `id`        INT(11)       NOT NULL AUTO_INCREMENT,
  `compra_id` INT(11)       NOT NULL,
  `metodo`    ENUM('Efectivo','TarjetaCredito','TarjetaDebito','Transferencia') NOT NULL,
  `monto`     DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pago_compra` (`compra_id`),
  CONSTRAINT `fk_pago_compra`
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- merchandising / compra_merchandising

CREATE TABLE `merchandising` (
  `id`           INT(11)       NOT NULL AUTO_INCREMENT,
  `concierto_id` INT(11)       NOT NULL,
  `nombre`       VARCHAR(100)  NOT NULL,
  `precio`       DECIMAL(10,2) NOT NULL,
  `stock`        INT(11)       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_merch_concierto` (`concierto_id`),
  CONSTRAINT `fk_merch_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `compra_merchandising` (
  `id`               INT(11)       NOT NULL AUTO_INCREMENT,
  `compra_id`        INT(11)       NOT NULL,
  `merchandising_id` INT(11)       NOT NULL,
  `cantidad`         INT(11)       NOT NULL,
  `precio_unitario`  DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cm_compra` (`compra_id`),
  KEY `fk_cm_merch` (`merchandising_id`),
  CONSTRAINT `fk_cm_compra`
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`),
  CONSTRAINT `fk_cm_merch`
    FOREIGN KEY (`merchandising_id`) REFERENCES `merchandising` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- estacionamiento / reserva_estacionamiento

CREATE TABLE `estacionamiento` (
  `id`           INT(11)       NOT NULL AUTO_INCREMENT,
  `concierto_id` INT(11)       NOT NULL,
  `ubicacion`    VARCHAR(100)  NOT NULL,
  `capacidad`    INT(11)       NOT NULL,
  `precio`       DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_estac_concierto` (`concierto_id`),
  CONSTRAINT `fk_estac_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `reserva_estacionamiento` (
  `id`                 INT(11) NOT NULL AUTO_INCREMENT,
  `compra_id`          INT(11) NOT NULL,
  `estacionamiento_id` INT(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_re_compra` (`compra_id`),
  KEY `fk_re_estac` (`estacionamiento_id`),
  CONSTRAINT `fk_re_compra`
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`),
  CONSTRAINT `fk_re_estac`
    FOREIGN KEY (`estacionamiento_id`) REFERENCES `estacionamiento` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- transporte / reserva_transporte

CREATE TABLE `transporte` (
  `id`           INT(11)       NOT NULL AUTO_INCREMENT,
  `concierto_id` INT(11)       NOT NULL,
  `tipo`         VARCHAR(50)   NOT NULL,
  `punto_salida` VARCHAR(100)  NOT NULL,
  `horario`      VARCHAR(50)   NOT NULL,
  `capacidad`    INT(11)       NOT NULL,
  `costo`        DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_trans_concierto` (`concierto_id`),
  CONSTRAINT `fk_trans_concierto`
    FOREIGN KEY (`concierto_id`) REFERENCES `concierto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `reserva_transporte` (
  `id`            INT(11) NOT NULL AUTO_INCREMENT,
  `compra_id`     INT(11) NOT NULL,
  `transporte_id` INT(11) NOT NULL,
  `asientos`      INT(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `fk_rt_compra` (`compra_id`),
  KEY `fk_rt_trans` (`transporte_id`),
  CONSTRAINT `fk_rt_compra`
    FOREIGN KEY (`compra_id`) REFERENCES `compra` (`id`),
  CONSTRAINT `fk_rt_trans`
    FOREIGN KEY (`transporte_id`) REFERENCES `transporte` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
