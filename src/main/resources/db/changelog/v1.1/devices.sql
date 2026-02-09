--liquibase formatted sql

--changeset Tatarinov A:035
--comment Create devices table
CREATE TABLE devices
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sn           VARCHAR(100) NOT NULL UNIQUE,
    type_id      UUID         NOT NULL,
    subtype_id   UUID         NOT NULL,
    created_time TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    is_deleted   BOOLEAN          DEFAULT FALSE,
    deleted_at   TIMESTAMP,

    CONSTRAINT fk_devices_type
        FOREIGN KEY (type_id)
            REFERENCES device_types (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_devices_subtype
        FOREIGN KEY (subtype_id)
            REFERENCES device_subtypes (id)
            ON DELETE RESTRICT
);
--rollback DELETE FROM devices;
