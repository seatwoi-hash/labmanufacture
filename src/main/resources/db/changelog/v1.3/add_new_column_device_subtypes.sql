--liquibase formatted sql

--changeset Tatarinov A:051
--comment Add is_side_one column to operations and link with device_subtypes
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name IN ('is_side_two');
SELECT COUNT(*) FROM information_schema.columns
WHERE table_name = 'device_subtypes'
  AND column_name IN ('is_side_two');

ALTER TABLE device_subtypes
    ADD COLUMN is_side_two BOOLEAN NOT NULL DEFAULT TRUE;

--rollback ALTER TABLE device_subtypes DROP COLUMN is_side_two;
