--liquibase formatted sql

--changeset Tatarinov A:018
--comment Create device_types table
CREATE TABLE device_types
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE device_types CASCADE;

--changeset Tatarinov A:019
--comment Insert device types
INSERT INTO device_types (name, description)
VALUES ('BOARD', 'Печатная плата'),
       ('DEVICE', 'Готовое изделие'),
       ('CABLE', 'Кабель для подключения');
--rollback DELETE FROM device_types;
