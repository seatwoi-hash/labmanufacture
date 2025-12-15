--liquibase formatted sql

--changeset Tatarinov A:005
CREATE TABLE roles
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    is_default  BOOLEAN          DEFAULT FALSE,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE roles CASCADE;

--changeset Tatarinov A:006
CREATE INDEX idx_roles_name ON roles (name);
--rollback DROP INDEX idx_roles_name;

--changeset Tatarinov A:007
CREATE INDEX idx_roles_is_default ON roles (is_default);
--rollback DROP INDEX idx_roles_is_default;

--changeset Tatarinov A:008
--comment Insert default roles
INSERT INTO roles (id, name, description, is_default)
VALUES (uuid_generate_v4(), 'user', 'Без задач', TRUE),
       (uuid_generate_v4(), 'admin', 'Администратор процесса', FALSE),
       (uuid_generate_v4(), 'quality', 'Контроль качества', FALSE),
       (uuid_generate_v4(), 'testerB', 'Тестировщик плат', FALSE),
       (uuid_generate_v4(), 'testerA', 'Тестировщик сборок', FALSE),
       (uuid_generate_v4(), 'operator', 'Оператор линии SMD', FALSE),
       (uuid_generate_v4(), 'output', 'Выводной монтаж', FALSE),
       (uuid_generate_v4(), 'varnisher', 'Лакировщик', FALSE),
       (uuid_generate_v4(), 'assembler', 'Сборщик', FALSE),
       (uuid_generate_v4(), 'repairman', 'Ремонтник', FALSE),
       (uuid_generate_v4(), 'washer', 'Мойщик', FALSE);
--rollback DELETE FROM roles WHERE name IN ('user', 'admin', 'quality', 'testerB', 'testerA', 'operator', 'output', 'varnisher', 'assembler', 'repairman', 'washer');
