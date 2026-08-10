--liquibase formatted sql

--changeset Tatarinov A:059
--comment Add binary data column to devices
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name = 'data';

ALTER TABLE device_subtypes
    ADD COLUMN data BYTEA;

--rollback ALTER TABLE device_subtypes DROP COLUMN IF EXISTS data;
