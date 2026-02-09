--liquibase formatted sql

--changeset Tatarinov A:037
--comment Add device_id column to operations and link with devices
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'operations' AND column_name = 'device_id'

ALTER TABLE operations
    ADD COLUMN device_id UUID;

-- Сначала добавляем связь по SN
UPDATE operations op
SET device_id = d.id
FROM devices d
WHERE op.sn = d.sn;

-- Теперь делаем колонку NOT NULL
ALTER TABLE operations
    ALTER COLUMN device_id SET NOT NULL;

-- Добавляем внешний ключ
ALTER TABLE operations
    ADD CONSTRAINT fk_operations_device
        FOREIGN KEY (device_id)
            REFERENCES devices (id)
            ON DELETE RESTRICT;

--rollback ALTER TABLE operations DROP CONSTRAINT fk_operations_device;
--rollback ALTER TABLE operations DROP COLUMN device_id;
