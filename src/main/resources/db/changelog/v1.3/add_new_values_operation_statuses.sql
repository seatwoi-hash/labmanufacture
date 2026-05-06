--liquibase formatted sql

--changeset Tatarinov A:052
--comment Add ОТК 5.1.1 operation statuses
INSERT INTO operation_statuses (name, description)
VALUES ('Quality_check_№5.1.1', 'Контроль качества №5.1 пройден'),
       ('Technical3', 'Технический 3')

--rollback DELETE FROM operation_statuses WHERE name = 'Quality_check_№5.1.1';
--rollback DELETE FROM operation_statuses WHERE name = 'Technical3';
