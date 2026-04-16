--liquibase formatted sql

--changeset Tatarinov_A:050
--comment Add url_pdf column to device_subtypes table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'device_subtypes' AND column_name = 'url_pdf'

ALTER TABLE device_subtypes
    ADD COLUMN url_pdf VARCHAR(250);

--rollback ALTER TABLE device_subtypes DROP COLUMN IF EXISTS url_pdf;
