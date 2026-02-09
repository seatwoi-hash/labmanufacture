--liquibase formatted sql

--changeset Tatarinov A:038
--comment Remove old columns from operations table
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'operations' AND column_name = 'device_id'

-- Удаляем старые колонки и constraints
ALTER TABLE operations
    DROP CONSTRAINT IF EXISTS fk_devices_type,
    DROP CONSTRAINT IF EXISTS fk_devices_subtype,
    DROP COLUMN sn,
    DROP COLUMN type_id,
    DROP COLUMN subtype_id

--rollback ALTER TABLE operations
--rollback     ADD COLUMN sn VARCHAR(100),
--rollback     ADD COLUMN type_id UUID,
--rollback     ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;
