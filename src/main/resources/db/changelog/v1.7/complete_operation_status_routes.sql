--liquibase formatted sql

--changeset Tatarinov A:064
--comment Add missing branched operation status routes for universal rollback
WITH route_data(previous_name, current_name, next_name, operation_name) AS (
    VALUES
        ('Quality_check_№2.1', 'Fail_test', 'Diagnostician_check_repair_№1', 'Диагностика №1'),
        ('Quality_check_№2.1', 'Fail_test', 'Diagnostician_check_test_№1', 'Диагностика №1'),
        ('Quality_check_№3', 'Fail_test', 'Diagnostician_check_repair_№1', 'Диагностика №1'),
        ('Quality_check_№3', 'Fail_test', 'Diagnostician_check_test_№1', 'Диагностика №1'),
        ('Diagnostician_check_test_№1', 'Fail_test', 'Diagnostician_check_repair_№1', 'Диагностика №1'),
        ('Diagnostician_check_test_№1', 'Fail_test', 'Diagnostician_check_test_№1', 'Диагностика №1'),
        ('Technical', 'Fail_test', 'Diagnostician_check_repair_№1', 'Диагностика №1'),
        ('Technical', 'Fail_test', 'Diagnostician_check_test_№1', 'Диагностика №1'),
        ('Quality_check_№4.1', 'Fail_test2', 'Diagnostician_check_repair_№2', 'Диагностика №2'),
        ('Quality_check_№4.1', 'Fail_test2', 'Diagnostician_check_test_№2', 'Диагностика №2'),
        ('Quality_check_№4.2', 'Fail_test2', 'Diagnostician_check_repair_№2', 'Диагностика №2'),
        ('Quality_check_№4.2', 'Fail_test2', 'Diagnostician_check_test_№2', 'Диагностика №2'),
        ('Diagnostician_check_test_№2', 'Fail_test2', 'Diagnostician_check_repair_№2', 'Диагностика №2'),
        ('Diagnostician_check_test_№2', 'Fail_test2', 'Diagnostician_check_test_№2', 'Диагностика №2'),
        ('Test2', 'Technical2', 'Washing1', 'Отмывка №1'),
        ('Fail_test2', 'Technical2', 'Washing1', 'Отмывка №1')
)
INSERT INTO operation_status_routes
    (previous_status_id, current_status_id, next_status_id, next_operation_name)
SELECT previous_status.id, current_status.id, next_status.id, route.operation_name
FROM route_data route
JOIN operation_statuses previous_status ON previous_status.name = route.previous_name
JOIN operation_statuses current_status ON current_status.name = route.current_name
JOIN operation_statuses next_status ON next_status.name = route.next_name
ON CONFLICT (previous_status_id, current_status_id, next_status_id) DO NOTHING;
--rollback DELETE FROM operation_status_routes route
--rollback USING operation_statuses previous_status, operation_statuses current_status
--rollback WHERE route.previous_status_id = previous_status.id
--rollback   AND route.current_status_id = current_status.id
--rollback   AND (previous_status.name, current_status.name) IN (
--rollback       ('Quality_check_№2.1', 'Fail_test'),
--rollback       ('Quality_check_№3', 'Fail_test'),
--rollback       ('Diagnostician_check_test_№1', 'Fail_test'),
--rollback       ('Technical', 'Fail_test'),
--rollback       ('Quality_check_№4.1', 'Fail_test2'),
--rollback       ('Quality_check_№4.2', 'Fail_test2'),
--rollback       ('Diagnostician_check_test_№2', 'Fail_test2'),
--rollback       ('Test2', 'Technical2'),
--rollback       ('Fail_test2', 'Technical2')
--rollback   );
