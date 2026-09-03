--liquibase formatted sql

--changeset Tatarinov A:070
--comment Store the original archive filename and MIME type
ALTER TABLE device_subtypes
    ADD COLUMN archive_original_name VARCHAR(512),
    ADD COLUMN archive_mime_type VARCHAR(127);

--rollback ALTER TABLE device_subtypes DROP COLUMN archive_mime_type, DROP COLUMN archive_original_name;
