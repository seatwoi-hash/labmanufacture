--liquibase formatted sql

--changeset Tatarinov A:073
--comment Create front assemblies linked to an assembly type and two boards
CREATE TABLE front_assemblies
(
    id                       UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id                  VARCHAR(100) NOT NULL,
    front_assembly_type_id   UUID         NOT NULL,
    motherboard_device_id    UUID         NOT NULL,
    keyboard_board_device_id UUID         NOT NULL,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at               TIMESTAMP,

    CONSTRAINT uq_front_assemblies_case_id UNIQUE (case_id),
    CONSTRAINT chk_front_assemblies_boards_differ
        CHECK (motherboard_device_id IS DISTINCT FROM keyboard_board_device_id),
    CONSTRAINT fk_front_assemblies_type
        FOREIGN KEY (front_assembly_type_id)
            REFERENCES front_assembly_types (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assemblies_motherboard
        FOREIGN KEY (motherboard_device_id)
            REFERENCES devices (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assemblies_keyboard_board
        FOREIGN KEY (keyboard_board_device_id)
            REFERENCES devices (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_front_assemblies_type
    ON front_assemblies (front_assembly_type_id);
CREATE INDEX idx_front_assemblies_motherboard
    ON front_assemblies (motherboard_device_id);
CREATE INDEX idx_front_assemblies_keyboard_board
    ON front_assemblies (keyboard_board_device_id);
CREATE INDEX idx_front_assemblies_is_deleted
    ON front_assemblies (is_deleted);

--rollback DROP TABLE front_assemblies CASCADE;
