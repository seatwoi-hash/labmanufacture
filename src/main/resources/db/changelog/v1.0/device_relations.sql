--liquibase formatted sql

--changeset Tatarinov A:029
--comment Create device_relations table
CREATE TABLE device_relations
(
    device_id_assembly UUID,
    device_id_part     UUID,
    quantity           INTEGER DEFAULT 1 NOT NULL CHECK (quantity > 0),

    CONSTRAINT check_devices_not_equal
        CHECK (device_id_assembly IS DISTINCT FROM device_id_part),

    CONSTRAINT pk_device_relations
        PRIMARY KEY (device_id_assembly, device_id_part),

    CONSTRAINT fk_device_relations_assembler
        FOREIGN KEY (device_id_assembly)
            REFERENCES devices (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_device_relations_part
        FOREIGN KEY (device_id_part)
            REFERENCES devices (id)
            ON DELETE CASCADE
);
--rollback DROP TABLE device_statuses CASCADE;
