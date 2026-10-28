-- =====================================================================
-- Manual schema for MySQL 8 / MariaDB.
-- Use this if Hibernate's ddl-auto: update did not create the table.
--   mysql -u root -p employee_db < db/schema-mysql.sql
-- Identifiers are back-quoted so keyword-like column names are safe.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS employee_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE employee_db;

CREATE TABLE IF NOT EXISTS `employees` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT,
    `first_name`      VARCHAR(50)   NOT NULL,
    `last_name`       VARCHAR(50)   NOT NULL,
    `email`           VARCHAR(120)  NOT NULL,
    `department`      VARCHAR(60)   NOT NULL,
    `position`        VARCHAR(60)   NOT NULL,
    `salary`          DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_employee_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Quick self-test: if this INSERT runs without error, the table and the
-- column names are fine and the 500 error is coming from the application side.
INSERT INTO `employees`
    (`first_name`, `last_name`, `email`, `department`, `position`,
     `salary`)
VALUES
    ('Test', 'User', 'test.user@example.com', 'Engineering', 'Developer',
     1000.00)
ON DUPLICATE KEY UPDATE `department` = VALUES(`department`);

SELECT * FROM `employees`;
