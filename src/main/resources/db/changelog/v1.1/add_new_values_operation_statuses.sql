--liquibase formatted sql

--changeset Tatarinov A:041
--comment Add ОТК 4.1 operation statuses
INSERT INTO operation_statuses (name, description)
VALUES ('Quality_check_№4.1', 'Контроль качества №4.1 пройден'),
       ('Quality_check_№4.2', 'Контроль качества №4.2 пройден'),
       ('Quality_check_№5.1', 'Контроль качества №5.1 пройден отправлен на мойку'),
       ('Quality_check_№1.1', 'Контроль качества №1.1 пройден'),
       ('Quality_check_№2.1', 'Контроль качества №2.1 пройден'),
       ('Fail_quality_check_№1.1', 'Контроль качества №1.1 не пройден'),
       ('Fail_quality_check_№2.1', 'Контроль качества №2.1 не пройден'),
       ('Fail_quality_check_№4.1', 'Контроль качества №4.1 не пройден'),
       ('Fail_quality_check_№4.2', 'Контроль качества №4.2 не пройден'),
       ('Fail_quality_check_№5.1', 'Контроль качества №5.1 не пройден отправлен в ремонт'),
       ('Fail_quality_check_№5.1.1', 'Контроль качества №5 не пройден отправлен на мойку'),
       ('Quality_check_№6', 'Контроль качества №6 пройден'),
       ('Fail_quality_check_№6', 'Контроль качества №6 не пройден'),
       ('Repair4', 'Ремонт №4'),
       ('Repair5', 'Ремонт №5'),
       ('Repair6', 'Ремонт №6'),
       ('Test2', 'Тест №2 пройден'),
       ('Fail_test2', 'Тест №2 не пройден'),
       ('Installation2', 'Выводной монтаж №2'),
       ('Technical', 'Технический 1'),
       ('Technical2', 'Технический 2')

--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№4.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№4.2';
--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№5.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№1.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№2.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№1.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№2.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№4.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№4.2';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№5.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№6';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_quality_check_№6';
--rollback DELETE FROM operation_statuses WHERE name = 'Repair4';
--rollback DELETE FROM operation_statuses WHERE name = 'Repair5';
--rollback DELETE FROM operation_statuses WHERE name = 'Repair6';
--rollback DELETE FROM operation_statuses WHERE name = 'Test2';
--rollback DELETE FROM operation_statuses WHERE name = 'Fail_test2';
--rollback DELETE FROM operation_statuses WHERE name = 'Installation2';
--rollback DELETE FROM operation_statuses WHERE name = 'technical';
--rollback DELETE FROM operation_statuses WHERE name = 'technical2';
