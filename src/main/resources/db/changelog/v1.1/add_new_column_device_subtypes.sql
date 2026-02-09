--liquibase formatted sql

--changeset Tatarinov A:044
--comment Add is_installation column to operations and link with device_subtypes
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name IN ('is_installation_one', 'is_test_two');
SELECT COUNT(*) FROM information_schema.columns
WHERE table_name = 'device_subtypes'
  AND column_name IN ('is_installation_one', 'is_test_two');

ALTER TABLE device_subtypes
    ADD COLUMN is_installation_one BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN is_test_two BOOLEAN NOT NULL DEFAULT TRUE;

--rollback ALTER TABLE device_subtypes DROP COLUMN is_installation_one, DROP COLUMN is_test_two;
