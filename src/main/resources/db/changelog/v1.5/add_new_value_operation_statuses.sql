--liquibase formatted sql

--changeset Tatarinov A:057
--comment Add diagnost operation statuses
INSERT INTO operation_statuses (name, description)
VALUES ('Diagnostician_check_test_№1', 'Диагностика №1 отправлен на тест'),
       ('Diagnostician_check_test_№2', 'Диагностика №2 отправлен на тест'),
       ('Diagnostician_check_repair_№1', 'Диагностика №1 отправлен в ремонт'),
       ('Diagnostician_check_repair_№2', 'Диагностика №2 отправлен в ремонт')

--rollback DELETE FROM operation_statuses WHERE name = 'Diagnostician_check_test_№1';
--rollback DELETE FROM operation_statuses WHERE name = 'Diagnostician_check_test_№2';
--rollback DELETE FROM operation_statuses WHERE name = 'Diagnostician_check_repair_№1';
--rollback DELETE FROM operation_statuses WHERE name = 'Diagnostician_check_repair_№2';
