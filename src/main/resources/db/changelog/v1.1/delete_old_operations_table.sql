--liquibase formatted sql

--changeset Tatarinov A:033
--comment Drop old operations table (renamed from devices)

-- Сначала переименуем старую таблицу
ALTER TABLE IF EXISTS operations RENAME TO old_operations;

-- Удаляем переименованную таблицу
DROP TABLE IF EXISTS old_operations CASCADE;

--rollback
--rollback -- Создать таблицу с данными невозможно без backup
--rollback -- Можно только создать пустую таблицу
--rollback CREATE TABLE operations (
--rollback     id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
--rollback     sn           VARCHAR(100) NOT NULL,
--rollback     type_id      UUID         NOT NULL,
--rollback     subtype_id   UUID         NOT NULL,
--rollback     description  TEXT,
--rollback     created_time TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
--rollback     account_id   UUID         NOT NULL,
--rollback     status_id    UUID         NOT NULL,
--rollback     is_deleted   BOOLEAN          DEFAULT FALSE,
--rollback     deleted_at   TIMESTAMP
--rollback );
