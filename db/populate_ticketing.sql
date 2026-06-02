-- Concert Ticketing System — test data
-- Run after create_ticketing.sql
-- Invoke:  mysql -u ticketing -p ticketing < db/populate_ticketing.sql


USE `ticketing`;


-- usuario
-- Plain-text passwords for reference:
--   admin@ticket.com   -> admin123
--   org@ticket.com     -> org123
--   acceso@ticket.com  -> acceso123
--   juan@mail.com      -> 1234
--   ana@mail.com       -> 1234
--
-- Hashes below verify against the plain-text passwords above

INSERT INTO `usuario` (`id`,`nombre`,`apellido`,`email`,`documento`,`password`,`rol`) VALUES
(1,'Admin','Sistema','admin@ticket.com','10000001','$2a$10$1aAj8WB5XQ3m35iMxWYGdO3UiAJ5KiJhVo58taJJ4yfNBXOb8Il4W','Administrador'),
(2,'Olivia','Ramos','org@ticket.com','20000002','$2a$10$ppGSC/rSZNModeQeSIthkOIpgkavyyTOrMlounf5gD7ecQTuxtLk.','Organizador'),
(3,'Pablo','Soto','acceso@ticket.com','30000003','$2a$10$PtSmhjLgc9eVTfkJDCOV3ODfsahd9l/z2WlkvXjc1COjo.UgJAHpW','PersonalAcceso'),
(4,'Juan','Perez','juan@mail.com','40000004','$2a$10$HO0Hn2e49ThOerz187Vwf.vvo8BCyQOUKO8BdAisagYfMuFYqILoG','Comprador'),
(5,'Ana','Gomez','ana@mail.com','40000005','$2a$10$HO0Hn2e49ThOerz187Vwf.vvo8BCyQOUKO8BdAisagYfMuFYqILoG','Comprador');

-- concierto

INSERT INTO `concierto` (`id`,`artista`,`fecha`,`hora`,`lugar`,`capacidad_total`,`organizador_id`,`estado`) VALUES
(1,'Coldplay','2026-06-15','21:00:00','Estadio Monumental',60,2,'Activo'),
(2,'Taylor Swift','2026-08-20','20:30:00','Estadio Velez',60,2,'Activo');

-- sector

INSERT INTO `sector` (`id`,`concierto_id`,`tipo`,`nombre`,`capacidad`,`precio`) VALUES
(1,1,'VIP','VIP Coldplay',10,200.00),
(2,1,'Platea','Platea Coldplay',20,100.00),
(3,1,'Campo','Campo Coldplay',30,50.00),
(4,2,'VIP','VIP Taylor',10,250.00),
(5,2,'Platea','Platea Taylor',20,120.00),
(6,2,'Campo','Campo Taylor',30,60.00);


-- ticket
-- Concierto 1 / VIP

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(1,1,'T-1-1-01',200.00,'Disponible'),(1,1,'T-1-1-02',200.00,'Disponible'),
(1,1,'T-1-1-03',200.00,'Disponible'),(1,1,'T-1-1-04',200.00,'Disponible'),
(1,1,'T-1-1-05',200.00,'Disponible'),(1,1,'T-1-1-06',200.00,'Disponible'),
(1,1,'T-1-1-07',200.00,'Disponible'),(1,1,'T-1-1-08',200.00,'Disponible'),
(1,1,'T-1-1-09',200.00,'Disponible'),(1,1,'T-1-1-10',200.00,'Disponible');

-- Concierto 1 / Platea

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(1,2,'T-1-2-01',100.00,'Disponible'),(1,2,'T-1-2-02',100.00,'Disponible'),
(1,2,'T-1-2-03',100.00,'Disponible'),(1,2,'T-1-2-04',100.00,'Disponible'),
(1,2,'T-1-2-05',100.00,'Disponible'),(1,2,'T-1-2-06',100.00,'Disponible'),
(1,2,'T-1-2-07',100.00,'Disponible'),(1,2,'T-1-2-08',100.00,'Disponible'),
(1,2,'T-1-2-09',100.00,'Disponible'),(1,2,'T-1-2-10',100.00,'Disponible'),
(1,2,'T-1-2-11',100.00,'Disponible'),(1,2,'T-1-2-12',100.00,'Disponible'),
(1,2,'T-1-2-13',100.00,'Disponible'),(1,2,'T-1-2-14',100.00,'Disponible'),
(1,2,'T-1-2-15',100.00,'Disponible'),(1,2,'T-1-2-16',100.00,'Disponible'),
(1,2,'T-1-2-17',100.00,'Disponible'),(1,2,'T-1-2-18',100.00,'Disponible'),
(1,2,'T-1-2-19',100.00,'Disponible'),(1,2,'T-1-2-20',100.00,'Disponible');

-- Concierto 1 / Campo

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(1,3,'T-1-3-01',50.00,'Disponible'),(1,3,'T-1-3-02',50.00,'Disponible'),
(1,3,'T-1-3-03',50.00,'Disponible'),(1,3,'T-1-3-04',50.00,'Disponible'),
(1,3,'T-1-3-05',50.00,'Disponible'),(1,3,'T-1-3-06',50.00,'Disponible'),
(1,3,'T-1-3-07',50.00,'Disponible'),(1,3,'T-1-3-08',50.00,'Disponible'),
(1,3,'T-1-3-09',50.00,'Disponible'),(1,3,'T-1-3-10',50.00,'Disponible'),
(1,3,'T-1-3-11',50.00,'Disponible'),(1,3,'T-1-3-12',50.00,'Disponible'),
(1,3,'T-1-3-13',50.00,'Disponible'),(1,3,'T-1-3-14',50.00,'Disponible'),
(1,3,'T-1-3-15',50.00,'Disponible'),(1,3,'T-1-3-16',50.00,'Disponible'),
(1,3,'T-1-3-17',50.00,'Disponible'),(1,3,'T-1-3-18',50.00,'Disponible'),
(1,3,'T-1-3-19',50.00,'Disponible'),(1,3,'T-1-3-20',50.00,'Disponible'),
(1,3,'T-1-3-21',50.00,'Disponible'),(1,3,'T-1-3-22',50.00,'Disponible'),
(1,3,'T-1-3-23',50.00,'Disponible'),(1,3,'T-1-3-24',50.00,'Disponible'),
(1,3,'T-1-3-25',50.00,'Disponible'),(1,3,'T-1-3-26',50.00,'Disponible'),
(1,3,'T-1-3-27',50.00,'Disponible'),(1,3,'T-1-3-28',50.00,'Disponible'),
(1,3,'T-1-3-29',50.00,'Disponible'),(1,3,'T-1-3-30',50.00,'Disponible');

-- Concierto 2 / VIP

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(2,4,'T-2-4-01',250.00,'Disponible'),(2,4,'T-2-4-02',250.00,'Disponible'),
(2,4,'T-2-4-03',250.00,'Disponible'),(2,4,'T-2-4-04',250.00,'Disponible'),
(2,4,'T-2-4-05',250.00,'Disponible'),(2,4,'T-2-4-06',250.00,'Disponible'),
(2,4,'T-2-4-07',250.00,'Disponible'),(2,4,'T-2-4-08',250.00,'Disponible'),
(2,4,'T-2-4-09',250.00,'Disponible'),(2,4,'T-2-4-10',250.00,'Disponible');

-- Concierto 2 / Platea

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(2,5,'T-2-5-01',120.00,'Disponible'),(2,5,'T-2-5-02',120.00,'Disponible'),
(2,5,'T-2-5-03',120.00,'Disponible'),(2,5,'T-2-5-04',120.00,'Disponible'),
(2,5,'T-2-5-05',120.00,'Disponible'),(2,5,'T-2-5-06',120.00,'Disponible'),
(2,5,'T-2-5-07',120.00,'Disponible'),(2,5,'T-2-5-08',120.00,'Disponible'),
(2,5,'T-2-5-09',120.00,'Disponible'),(2,5,'T-2-5-10',120.00,'Disponible'),
(2,5,'T-2-5-11',120.00,'Disponible'),(2,5,'T-2-5-12',120.00,'Disponible'),
(2,5,'T-2-5-13',120.00,'Disponible'),(2,5,'T-2-5-14',120.00,'Disponible'),
(2,5,'T-2-5-15',120.00,'Disponible'),(2,5,'T-2-5-16',120.00,'Disponible'),
(2,5,'T-2-5-17',120.00,'Disponible'),(2,5,'T-2-5-18',120.00,'Disponible'),
(2,5,'T-2-5-19',120.00,'Disponible'),(2,5,'T-2-5-20',120.00,'Disponible');

-- Concierto 2 / Campo

INSERT INTO `ticket` (`concierto_id`,`sector_id`,`codigo`,`precio`,`estado`) VALUES
(2,6,'T-2-6-01',60.00,'Disponible'),(2,6,'T-2-6-02',60.00,'Disponible'),
(2,6,'T-2-6-03',60.00,'Disponible'),(2,6,'T-2-6-04',60.00,'Disponible'),
(2,6,'T-2-6-05',60.00,'Disponible'),(2,6,'T-2-6-06',60.00,'Disponible'),
(2,6,'T-2-6-07',60.00,'Disponible'),(2,6,'T-2-6-08',60.00,'Disponible'),
(2,6,'T-2-6-09',60.00,'Disponible'),(2,6,'T-2-6-10',60.00,'Disponible'),
(2,6,'T-2-6-11',60.00,'Disponible'),(2,6,'T-2-6-12',60.00,'Disponible'),
(2,6,'T-2-6-13',60.00,'Disponible'),(2,6,'T-2-6-14',60.00,'Disponible'),
(2,6,'T-2-6-15',60.00,'Disponible'),(2,6,'T-2-6-16',60.00,'Disponible'),
(2,6,'T-2-6-17',60.00,'Disponible'),(2,6,'T-2-6-18',60.00,'Disponible'),
(2,6,'T-2-6-19',60.00,'Disponible'),(2,6,'T-2-6-20',60.00,'Disponible'),
(2,6,'T-2-6-21',60.00,'Disponible'),(2,6,'T-2-6-22',60.00,'Disponible'),
(2,6,'T-2-6-23',60.00,'Disponible'),(2,6,'T-2-6-24',60.00,'Disponible'),
(2,6,'T-2-6-25',60.00,'Disponible'),(2,6,'T-2-6-26',60.00,'Disponible'),
(2,6,'T-2-6-27',60.00,'Disponible'),(2,6,'T-2-6-28',60.00,'Disponible'),
(2,6,'T-2-6-29',60.00,'Disponible'),(2,6,'T-2-6-30',60.00,'Disponible');


-- merchandising

INSERT INTO `merchandising` (`concierto_id`,`nombre`,`precio`,`stock`) VALUES
(1,'Remera Coldplay',35.00,100),
(1,'Poster Coldplay',15.00,200),
(2,'Remera Taylor',40.00,100),
(2,'Poster Taylor',18.00,200);


-- estacionamiento

INSERT INTO `estacionamiento` (`concierto_id`,`ubicacion`,`capacidad`,`precio`) VALUES
(1,'Estacionamiento Norte',50,10.00),
(1,'Estacionamiento Sur',50,10.00),
(2,'Estacionamiento Velez Este',60,12.00);


-- transporte

INSERT INTO `transporte` (`concierto_id`,`tipo`,`punto_salida`,`horario`,`capacidad`,`costo`) VALUES
(1,'Camioneta','Obelisco','19:30',15,25.00),
(1,'Camioneta','Plaza Italia','19:30',15,25.00),
(2,'Camioneta','Obelisco','19:00',15,28.00);
