--liquibase formatted sql

--changeset Tatarinov A:074
--comment Create operation history for front assemblies
CREATE TABLE front_assembly_operations
(
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    front_assembly_id        UUID      NOT NULL,
    account_id               UUID      NOT NULL,
    status_id                UUID      NOT NULL,
    description              TEXT,
    created_time             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted               BOOLEAN   NOT NULL DEFAULT FALSE,
    deleted_at               TIMESTAMP,
    is_rollback              BOOLEAN   NOT NULL DEFAULT FALSE,
    rolled_back_operation_id UUID,

    CONSTRAINT fk_front_assembly_operations_assembly
        FOREIGN KEY (front_assembly_id)
            REFERENCES front_assemblies (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assembly_operations_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assembly_operations_status
        FOREIGN KEY (status_id)
            REFERENCES operation_statuses (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assembly_operations_rollback
        FOREIGN KEY (rolled_back_operation_id)
            REFERENCES front_assembly_operations (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_front_assembly_operations_assembly
    ON front_assembly_operations (front_assembly_id);
CREATE INDEX idx_front_assembly_operations_account
    ON front_assembly_operations (account_id);
CREATE INDEX idx_front_assembly_operations_status
    ON front_assembly_operations (status_id);
CREATE INDEX idx_front_assembly_operations_active
    ON front_assembly_operations (front_assembly_id, is_deleted);
CREATE INDEX idx_front_assembly_operations_created_time
    ON front_assembly_operations (created_time);

--rollback DROP TABLE front_assembly_operations CASCADE;
