--liquibase formatted sql

--changeset Tatarinov A:048
--comment Remove NOT NULL constraint from subtype_id column in devices table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'devices' AND column_name = 'subtype_id' AND is_nullable = 'NO'

ALTER TABLE devices
    ALTER COLUMN subtype_id DROP NOT NULL;
--rollback ALTER TABLE devices ALTER COLUMN subtype_id SET NOT NULL;
