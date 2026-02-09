--liquibase formatted sql

--changeset Tatarinov A:039
--comment Update index name to reflect new structure
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'operations' AND indexname = 'idx_accounts_sn'

-- Переименовываем индекс или создаем новый для device_id
DROP INDEX idx_accounts_sn;

CREATE INDEX idx_operations_device_id ON operations (device_id);

--rollback DROP INDEX IF EXISTS idx_operations_device_id;
--rollback CREATE INDEX idx_accounts_sn ON operations (sn);
