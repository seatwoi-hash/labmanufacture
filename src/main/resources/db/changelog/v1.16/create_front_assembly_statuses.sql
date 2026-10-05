--liquibase formatted sql

--changeset Tatarinov A:075
--comment Create statuses dedicated to front assembly operations
CREATE TABLE front_assembly_statuses
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_front_assembly_statuses_name UNIQUE (name)
);

INSERT INTO front_assembly_statuses (name, description)
VALUES ('created', 'Создан'),
       ('Quality_check_1', 'ОТК №1'),
       ('Repair1', 'Ремонт №1'),
       ('Test1', 'Тестирование №1'),
       ('Diagnostician', 'Диагностика'),
       ('Quality_check_2', 'ОТК №2'),
       ('Repair2', 'Ремонт №2'),
       ('Test2', 'Тестирование №2'),
       ('Quality_check_3', 'ОТК №3'),
       ('ready', 'Готов');

ALTER TABLE front_assembly_operations
    DROP CONSTRAINT fk_front_assembly_operations_status;

ALTER TABLE front_assembly_operations
    ADD CONSTRAINT fk_front_assembly_operations_status
        FOREIGN KEY (status_id)
            REFERENCES front_assembly_statuses (id)
            ON DELETE RESTRICT;

--rollback ALTER TABLE front_assembly_operations DROP CONSTRAINT fk_front_assembly_operations_status;
--rollback ALTER TABLE front_assembly_operations ADD CONSTRAINT fk_front_assembly_operations_status FOREIGN KEY (status_id) REFERENCES operation_statuses (id) ON DELETE RESTRICT;
--rollback DROP TABLE front_assembly_statuses CASCADE;
