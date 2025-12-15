--liquibase formatted sql

--changeset Tatarinov A:020
--comment Create device_subtypes table
CREATE TABLE device_subtypes
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN          DEFAULT FALSE
);
--rollback DROP TABLE device_subtypes CASCADE;
