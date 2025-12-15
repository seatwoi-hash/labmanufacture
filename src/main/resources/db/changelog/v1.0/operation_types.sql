--liquibase formatted sql

--changeset Tatarinov A:024
--comment Create operation_types table
CREATE TABLE operation_types
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE operation_types CASCADE;

--changeset Tatarinov A:026
--comment Insert device statuses
INSERT INTO operation_types (name, description)
VALUES ('created', 'Создан'),
       ('Side1', 'Сторона 1'),
       ('Side2', 'Сторона 2'),
       ('Installation', 'Выводной монтаж'),
       ('Washing1', 'Отмывка №1'),
       ('Washing2', 'Отмывка №2'),
       ('Quality_check_№1', 'Контроль качества №1 пройден'),
       ('Quality_check_№2', 'Контроль качества №2 пройден'),
       ('Quality_check_№3', 'Контроль качества №3 пройден'),
       ('Quality_check_№4', 'Контроль качества №4 пройден'),
       ('Quality_check_№5', 'Контроль качества №5 пройден'),
       ('Fail_quality_check_№1', 'Контроль качества №1 не пройден'),
       ('Fail_quality_check_№2', 'Контроль качества №2 не пройден'),
       ('Fail_quality_check_№3', 'Контроль качества №3 не пройден'),
       ('Fail_quality_check_№4', 'Контроль качества №4 не пройден'),
       ('Fail_quality_check_№5', 'Контроль качества №5 не пройден'),
       ('Test', 'Тестировка'),
       ('Fail_test', 'Тестировка'),
       ('Repair', 'Ремонт №1'),
       ('Repair', 'Ремонт №2'),
       ('Repair', 'Ремонт №3'),
       ('Varnish', 'Нанесение компаунда'),
       ('not_ready', 'Не готов');
--rollback DELETE FROM operation_types;
