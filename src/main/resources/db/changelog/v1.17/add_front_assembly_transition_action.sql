--liquibase formatted sql

--changeset Tatarinov A:076
--comment Store the action that created each front assembly operation
--validCheckSum: 9:52ef5545fd911c4e31f8e31110148482
ALTER TABLE front_assembly_operations
    ADD COLUMN transition_action VARCHAR(32);
--rollback ALTER TABLE front_assembly_operations DROP COLUMN transition_action;
