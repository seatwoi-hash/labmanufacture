--liquibase formatted sql

--changeset Tatarinov A:072
--comment Create front assembly types with board subtype references and optional PDF documents
CREATE TABLE front_assembly_types
(
    id                             UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                           VARCHAR(100) NOT NULL,
    description                    TEXT         NOT NULL,
    motherboard_subtype_id         UUID         NOT NULL,
    keyboard_board_subtype_id      UUID         NOT NULL,
    assembly_instruction           BYTEA,
    assembly_instruction_file_name VARCHAR(512),
    assembly_instruction_mime_type VARCHAR(127),
    assembly_diagram               BYTEA,
    assembly_diagram_file_name     VARCHAR(512),
    assembly_diagram_mime_type     VARCHAR(127),
    created_at                     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted                     BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_front_assembly_types_name UNIQUE (name),
    CONSTRAINT fk_front_assembly_types_motherboard_subtype
        FOREIGN KEY (motherboard_subtype_id)
            REFERENCES device_subtypes (id)
            ON DELETE RESTRICT,
    CONSTRAINT fk_front_assembly_types_keyboard_subtype
        FOREIGN KEY (keyboard_board_subtype_id)
            REFERENCES device_subtypes (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_front_assembly_types_motherboard_subtype
    ON front_assembly_types (motherboard_subtype_id);
CREATE INDEX idx_front_assembly_types_keyboard_subtype
    ON front_assembly_types (keyboard_board_subtype_id);
CREATE INDEX idx_front_assembly_types_is_deleted
    ON front_assembly_types (is_deleted);

--rollback DROP TABLE front_assembly_types CASCADE;
