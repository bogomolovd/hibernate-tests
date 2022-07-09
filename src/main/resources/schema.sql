CREATE TABLE person (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR NOT NULL,
    `surname` VARCHAR NOT NULL,
    `nicknames` VARCHAR,
    `ssn` VARCHAR NOT NULL UNIQUE,
    PRIMARY KEY (`id`)
);

CREATE TABLE person_last_visited_cities (
    `person_id` BIGINT NOT NULL,
    `last_visited_cities` VARCHAR NOT NULL
);


CREATE TABLE phone (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `number` VARCHAR NOT NULL,
    `person_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY(`person_id`) references person(`id`)
);

CREATE TABLE phone_details (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `operator` VARCHAR NOT NULL,
    `phone_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY(`phone_id`) references phone(`id`)
);

CREATE TABLE apartment (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `city` VARCHAR NOT NULL,
    `street` VARCHAR NOT NULL,
    `building` INT NOT NULL,
    `apartment` INT NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE person_apartment (
    `owner_id` BIGINT NOT NULL,
    `apartment_id` VARCHAR NOT NULL,
    PRIMARY KEY (`owner_id`, `apartment_id`),
    FOREIGN KEY(`owner_id`) references person(`id`),
    FOREIGN KEY(`apartment_id`) references apartment(`id`)
);

CREATE TABLE account (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `owner` VARCHAR NOT NULL,
    `balance` NUMERIC NOT NULL,
    `interest_rate` NUMERIC NOT NULL,
    `overdraft_fee` NUMERIC,
    `credit_limit` NUMERIC,
    `active` BOOLEAN,
    `version` SMALLINT NOT NULL,
    `DTYPE` VARCHAR(31) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE SEQUENCE id_seq;
