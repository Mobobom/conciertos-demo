#!/usr/bin/env bash
# Creates the `ticketing` MySQL database and dedicated user.
# Run once with sudo: sudo ./db/setup_mysql_ticketing.sh
set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "Run with sudo: sudo $0" >&2
  exit 1
fi

mysql -u root <<'SQL'
CREATE DATABASE IF NOT EXISTS `ticketing` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER IF NOT EXISTS 'ticketing'@'localhost' IDENTIFIED BY 'Db_ticketing_2026!';
ALTER  USER             'ticketing'@'localhost' IDENTIFIED BY 'Db_ticketing_2026!';

GRANT ALL PRIVILEGES ON `ticketing`.* TO 'ticketing'@'localhost';
FLUSH PRIVILEGES;

SELECT User, Host FROM mysql.user WHERE User = 'ticketing';
SHOW DATABASES LIKE 'ticketing';
SQL

echo "Done."
