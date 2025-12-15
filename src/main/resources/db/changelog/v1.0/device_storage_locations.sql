--liquibase formatted sql

--changeset Tatarinov A:032
--comment Create device_statuses table
CREATE TABLE device_storage_locations
(
    location_id       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id         UUID                          NOT NULL,
    location_types_id UUID                          NOT NULL,
    is_current        BOOLEAN          DEFAULT TRUE NOT NULL,
    created_time      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_device_storage_device
        FOREIGN KEY (device_id)
            REFERENCES devices (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_device_storage_location_types
        FOREIGN KEY (location_types_id)
            REFERENCES location_types (id)
            ON DELETE CASCADE
);
--rollback DELETE FROM device_storage_locations;
