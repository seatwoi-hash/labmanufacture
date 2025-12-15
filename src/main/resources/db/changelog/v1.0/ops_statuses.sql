--liquibase formatted sql

--changeset Tatarinov A:024
--comment Create device_statuses table
CREATE TABLE ops_statuses
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE device_statuses CASCADE;

--changeset Tatarinov A:025
--comment Insert device statuses
INSERT INTO ops_statuses (name, description)
VALUES ('ready', 'Готово'),
       ('not_required', 'Не требуется');
--rollback DELETE FROM device_statuses;
