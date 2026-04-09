--liquibase formatted sql

--changeset Tatarinov A:046
--comment Add is_installation column to operations and link with devices
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name = 'sn_type'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name = 'version_type'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name = 'file_name'

SELECT COUNT(*) FROM information_schema.columns
WHERE table_name = 'device_subtypes'
  AND column_name IN ('sn_type', 'version_type', 'file_name');

ALTER TABLE device_subtypes
    ADD COLUMN sn_type int NOT NULL default 0,
    ADD COLUMN version_type int NOT NULL default 0,
    ADD COLUMN file_name varchar(100);
--rollback ALTER TABLE device_subtypes DROP COLUMN IF EXISTS sn_type, DROP COLUMN IF EXISTS version_type, DROP COLUMN IF EXISTS file_name;
