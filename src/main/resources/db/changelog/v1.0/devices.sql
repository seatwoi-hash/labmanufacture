--liquibase formatted sql

--changeset Tatarinov A:022
--comment Create devices table
CREATE TABLE devices
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sn           VARCHAR(100) NOT NULL,
    type_id      UUID         NOT NULL,
    subtype_id   UUID         NOT NULL,
    description  TEXT,
    created_time TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    account_id   UUID         NOT NULL,
    status_id    UUID         NOT NULL,
    is_deleted   BOOLEAN          DEFAULT FALSE,
    deleted_at   TIMESTAMP,

    CONSTRAINT fk_devices_type
        FOREIGN KEY (type_id)
            REFERENCES device_types (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_devices_subtype
        FOREIGN KEY (subtype_id)
            REFERENCES device_subtypes (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_devices_status
        FOREIGN KEY (status_id)
            REFERENCES device_statuses (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_accounts
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE RESTRICT
);
--rollback DELETE FROM devices;

--changeset Tatarinov A:023
CREATE INDEX idx_accounts_sn ON devices (sn);
--rollback DROP INDEX idx_accounts_sn;
