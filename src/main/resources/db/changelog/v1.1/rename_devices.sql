--liquibase formatted sql

--changeset Tatarinov A:034
--comment Rename devices table to operations
ALTER TABLE devices RENAME TO operations;

--rollback ALTER TABLE operations RENAME TO devices;
