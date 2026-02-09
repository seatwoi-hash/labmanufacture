--liquibase formatted sql

--changeset Tatarinov A:040
--comment Rename device_statuses table to operation_statuses
ALTER TABLE device_statuses RENAME TO operation_statuses;

--rollback ALTER TABLE operation_statuses RENAME TO device_statuses;
