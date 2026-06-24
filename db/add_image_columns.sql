-- D1 image columns migration
-- Run this on an existing ticketing database without recreating all data:
-- mysql -u ticketing -p ticketing < db/add_image_columns.sql

USE `ticketing`;

ALTER TABLE `concierto`
  ADD COLUMN `poster_url` VARCHAR(255) DEFAULT NULL;

ALTER TABLE `merchandising`
  ADD COLUMN `imagen_url` VARCHAR(255) DEFAULT NULL;

UPDATE `concierto`
SET `poster_url` = CASE `id`
  WHEN 1 THEN 'concerts/coldplay-2026-poster.png'
  WHEN 2 THEN 'concerts/taylor-swift-2026-poster.png'
  ELSE `poster_url`
END
WHERE `poster_url` IS NULL;

UPDATE `merchandising`
SET `imagen_url` = CASE `nombre`
  WHEN 'Remera Coldplay' THEN 'merch/coldplay-2026-remera.png'
  WHEN 'Poster Coldplay' THEN 'merch/coldplay-2026-poster.png'
  WHEN 'Remera Taylor' THEN 'merch/taylor-swift-2026-remera.png'
  WHEN 'Poster Taylor' THEN 'merch/taylor-swift-2026-poster.png'
  ELSE `imagen_url`
END
WHERE `imagen_url` IS NULL;
