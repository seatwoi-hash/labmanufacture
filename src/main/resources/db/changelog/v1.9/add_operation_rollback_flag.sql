--liquibase formatted sql

--changeset Tatarinov A:066
--comment Add explicit rollback marker to operation history
ALTER TABLE operations
    ADD COLUMN is_rollback BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE operations
SET is_rollback = TRUE
WHERE description LIKE 'Возвращён -%';

--rollback ALTER TABLE operations DROP COLUMN is_rollback;
