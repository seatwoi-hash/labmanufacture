--liquibase formatted sql

--changeset Tatarinov A:016
--comment Create device_statuses table
CREATE TABLE device_statuses
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(50),
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);
--rollback DROP TABLE device_statuses CASCADE;

--changeset Tatarinov A:017
--comment Insert device statuses
INSERT INTO device_statuses (name, description)
VALUES ('ready', 'Готово'),
       ('created', 'Создан'),
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
       ('Repair1', 'Ремонт №1'),
       ('Repair2', 'Ремонт №2'),
       ('Repair3', 'Ремонт №3'),
       ('Varnish', 'Нанесение компаунда'),
       ('not_ready', 'Не готов');
--rollback DELETE FROM device_statuses;
