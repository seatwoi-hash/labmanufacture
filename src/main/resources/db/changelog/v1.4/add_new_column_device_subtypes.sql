--liquibase formatted sql

--changeset Tatarinov A:053
--comment Add is_installation column to operations and link with devices
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'devices' AND column_name = 'url_pdf_read'
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'devices' AND column_name = 'url_txt_read'

SELECT COUNT(*) FROM information_schema.columns
WHERE table_name = 'devices'
  AND column_name IN ('url_pdf_read', 'url_txt_read');

ALTER TABLE devices
    ADD COLUMN url_pdf_read VARCHAR(250),
    ADD COLUMN url_txt_read VARCHAR(250);
--rollback ALTER TABLE device_subtypes DROP COLUMN IF EXISTS url_pdf_read, DROP COLUMN IF EXISTS url_txt_read;
