--liquibase formatted sql

--changeset Tatarinov A:045
--comment Create file_data table

CREATE TABLE file_data
(
    id            UUID PRIMARY KEY   DEFAULT uuid_generate_v4(),
    original_name VARCHAR(512),
    new_name      VARCHAR(512),
    mime_type     VARCHAR(127),
    data          BYTEA,
    operations_id UUID      NOT NULL UNIQUE,
    account_id    UUID      NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_id_file
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_operations_id_file
        FOREIGN KEY (operations_id)
            REFERENCES operations (id)
            ON DELETE RESTRICT
);
--rollback DELETE FROM file_data;
