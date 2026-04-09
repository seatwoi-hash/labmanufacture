--liquibase formatted sql

--changeset Tatarinov A:047
--comment Add is_installation column to operations and link with devices
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'devices' AND column_name = 'url_pdf'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'devices' AND column_name = 'url_txt'

SELECT COUNT(*) FROM information_schema.columns
WHERE table_name = 'devices'
  AND column_name IN ('url_pdf', 'url_txt');

ALTER TABLE devices
    ADD COLUMN url_pdf VARCHAR(250),
    ADD COLUMN url_txt VARCHAR(250);
--rollback ALTER TABLE device_subtypes DROP COLUMN IF EXISTS url_pdf, DROP COLUMN IF EXISTS url_txt;
