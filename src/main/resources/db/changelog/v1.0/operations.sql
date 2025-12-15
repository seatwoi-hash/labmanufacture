--liquibase formatted sql

--changeset Tatarinov A:027
--comment Create operations table
CREATE TABLE operations
(
    id                 UUID PRIMARY KEY   DEFAULT uuid_generate_v4(),
    device_id          UUID      NOT NULL,
    operation_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id         UUID      NOT NULL,
    operation_types_id UUID      NOT NULL,
    ops_statuses_id    UUID,
    description        TEXT,
    is_deleted         BOOLEAN   NOT NULL DEFAULT FALSE,
    deleted_at         TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_devices
        FOREIGN KEY (device_id)
            REFERENCES devices (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_operation_types
        FOREIGN KEY (operation_types_id)
            REFERENCES operation_types (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_accounts
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ops_statuses
            FOREIGN KEY (ops_statuses_id)
            REFERENCES ops_statuses (id)
            ON DELETE RESTRICT
);
--rollback DROP TABLE operations CASCADE;

--changeset Tatarinov A:028
CREATE INDEX idx_device_id ON operations (device_id);
--rollback DROP INDEX idx_device_id;
