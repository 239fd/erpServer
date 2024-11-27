CREATE DATABASE IF NOT EXISTS mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE mydb;

CREATE TABLE IF NOT EXISTS `mydb`.`suppliers` (
                                                  `id` INT NOT NULL AUTO_INCREMENT,
                                                  `INN` VARCHAR(9) NOT NULL,
                                                  `name` VARCHAR(45) NOT NULL,
                                                  `address` VARCHAR(45) NOT NULL,
                                                  `latitude` DOUBLE,  -- Поле для широты
                                                  `longitude` DOUBLE, -- Поле для долготы
                                                  PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`products` (
                                                 `id` INT NOT NULL AUTO_INCREMENT,
                                                 `height` DOUBLE NOT NULL,
                                                 `length` DOUBLE NOT NULL,
                                                 `name` VARCHAR(255) NOT NULL,
                                                 `price` DOUBLE NOT NULL,
                                                 `status` ENUM('accepted', 'nonverified', 'writeoff') NOT NULL,
                                                 `unit` VARCHAR(255) NOT NULL,
                                                 `width` DOUBLE NOT NULL,
                                                 `weight` DOUBLE NOT NULL,
                                                 `bestbeforedate` DATE NOT NULL,
                                                 `amount` INT NOT NULL,
                                                 PRIMARY KEY (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`stocks` (
                                               `id` INT NOT NULL AUTO_INCREMENT,
                                               `amount` INT NOT NULL,
                                               `suppliers_id` INT NOT NULL,
                                               `products_id` INT NOT NULL,
                                               PRIMARY KEY (`id`, `suppliers_id`, `products_id`),
                                               FOREIGN KEY (`suppliers_id`) REFERENCES `mydb`.`suppliers` (`id`),
                                               FOREIGN KEY (`products_id`) REFERENCES `mydb`.`products` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`organization` (
                                                     `id` INT NOT NULL AUTO_INCREMENT,
                                                     `INN` VARCHAR(9) NOT NULL,
                                                     `name` VARCHAR(45) NOT NULL,
                                                     `address` VARCHAR(255) NOT NULL,  -- увеличен размер для возможных длинных адресов
                                                     `latitude` DOUBLE,  -- Поле для широты
                                                     `longitude` DOUBLE, -- Поле для долготы
                                                     PRIMARY KEY (`id`),
                                                     UNIQUE INDEX `INN_UNIQUE` (`INN` ASC)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`warehouse` (
                                                  `id` INT NOT NULL AUTO_INCREMENT,
                                                  `name` VARCHAR(45) NOT NULL,
                                                  `address` VARCHAR(255) NOT NULL,  -- увеличен размер для возможных длинных адресов
                                                  `latitude` DOUBLE,  -- Поле для широты
                                                  `longitude` DOUBLE, -- Поле для долготы
                                                  `organizationid` INT NOT NULL,
                                                  PRIMARY KEY (`id`),
                                                  FOREIGN KEY (`organizationid`) REFERENCES `mydb`.`organization` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`rack` (
                                             `id` INT NOT NULL AUTO_INCREMENT,
                                             `capacity` INT NOT NULL,
                                             `warehouse_id` INT NOT NULL,
                                             PRIMARY KEY (`id`),
                                             FOREIGN KEY (`warehouse_id`) REFERENCES `mydb`.`warehouse` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`cell` (
                                             `id` INT NOT NULL AUTO_INCREMENT,
                                             `length` DOUBLE NOT NULL,
                                             `width` DOUBLE NOT NULL,
                                             `height` DOUBLE NOT NULL,
                                             `rack_id` INT NOT NULL,
                                             PRIMARY KEY (`id`),
                                             FOREIGN KEY (`rack_id`) REFERENCES `mydb`.`rack` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`employees` (
                                                  `id` INT NOT NULL AUTO_INCREMENT,
                                                  `login` VARCHAR(255) NOT NULL,
                                                  `first_name` VARCHAR(255) NOT NULL,
                                                  `password` VARCHAR(255) NOT NULL,
                                                  `phone` VARCHAR(255) NOT NULL,
                                                  `second_name` VARCHAR(255) NOT NULL,
                                                  `surname` VARCHAR(255) NULL DEFAULT NULL,
                                                  `title` ENUM('ROLE_DIRECTOR', 'ROLE_ACCOUNTANT', 'ROLE_WORKER', 'ROLE_MANAGER') NOT NULL,
                                                  `organization_id` INT ,
                                                  `warehouse_id` INT ,
                                                  PRIMARY KEY (`id`),
                                                  FOREIGN KEY (`organization_id`) REFERENCES `mydb`.`organization` (`id`),
                                                  FOREIGN KEY (`warehouse_id`) REFERENCES `mydb`.`warehouse` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `mydb`.`cell_has_products` (
                                                          `cell_id` INT NOT NULL,
                                                          `products_id` INT NOT NULL,
                                                          `products_suppliers_id` INT NOT NULL,
                                                          PRIMARY KEY (`cell_id`, `products_id`, `products_suppliers_id`),
                                                          FOREIGN KEY (`cell_id`) REFERENCES `mydb`.`cell` (`id`),
                                                          FOREIGN KEY (`products_id`) REFERENCES `mydb`.`products` (`id`)
) ENGINE = InnoDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `mydb`.`cell_has_products`
    MODIFY COLUMN `products_suppliers_id` INT NOT NULL DEFAULT 0;

ALTER TABLE products MODIFY name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE organization MODIFY address VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE warehouse MODIFY address VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE suppliers MODIFY address VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE `mydb`.`stocks`
    ADD COLUMN `price` DOUBLE NOT NULL;
