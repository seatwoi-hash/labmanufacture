--liquibase formatted sql

--changeset Tatarinov A:062
--comment Create operation status routes table
CREATE TABLE operation_status_routes
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    previous_status_id  UUID REFERENCES operation_statuses (id) ON DELETE RESTRICT,
    current_status_id   UUID NOT NULL REFERENCES operation_statuses (id) ON DELETE RESTRICT,
    next_status_id      UUID REFERENCES operation_statuses (id) ON DELETE RESTRICT,
    next_operation_name VARCHAR(100) NOT NULL,
    CONSTRAINT uk_operation_status_route
        UNIQUE (previous_status_id, current_status_id, next_status_id)
);

CREATE INDEX idx_operation_status_routes_current
    ON operation_status_routes (current_status_id);
CREATE INDEX idx_operation_status_routes_next
    ON operation_status_routes (next_status_id);
--rollback DROP TABLE operation_status_routes;

--changeset Tatarinov A:063
--comment Insert current production status routes
WITH route_data(previous_name, current_name, next_name, operation_name) AS (
    VALUES
        (NULL, 'created', 'Side1', 'Монтаж "Сторона 1"'),
        ('created', 'Side1', 'Side2', 'Монтаж "Сторона 2"'),
        ('Side1', 'Side2', 'Quality_check_№1', 'ОТК №1'),
        ('Side2', 'Quality_check_№1', 'Installation1', 'Выводной монтаж №1'),
        ('Side2', 'Fail_quality_check_№1', 'Repair1', 'Ремонт №1'),
        ('Fail_quality_check_№1', 'Repair1', 'Quality_check_№1.1', 'ОТК №1.1'),
        ('Repair1', 'Quality_check_№1.1', 'Installation1', 'Выводной монтаж №1'),
        ('Repair1', 'Fail_quality_check_№1.1', 'Repair1', 'Ремонт №1'),
        ('Quality_check_№1', 'Installation1', 'Quality_check_№2', 'ОТК №2'),
        ('Installation1', 'Quality_check_№2', 'Test', 'Тестировка'),
        ('Installation1', 'Fail_quality_check_№2', 'Repair2', 'Ремонт №2'),
        ('Repair2', 'Quality_check_№2.1', 'Test', 'Тестировка'),
        ('Repair2', 'Fail_quality_check_№2.1', 'Repair2', 'Ремонт №2'),
        ('Fail_quality_check_№2', 'Repair2', 'Quality_check_№2.1', 'ОТК №2.1'),
        ('Quality_check_№2', 'Test', 'Installation2', 'Выводной монтаж №2'),
        ('Quality_check_№2', 'Fail_test', 'Diagnostician_check_repair_№1', 'Диагностика №1'),
        ('Quality_check_№2', 'Fail_test', 'Diagnostician_check_test_№1', 'Диагностика №1'),
        ('Repair3', 'Quality_check_№3', 'Test', 'Тестировка'),
        ('Repair3', 'Fail_quality_check_№3', 'Repair3', 'Ремонт №3'),
        ('Test', 'Installation2', 'Quality_check_№4', 'ОТК №4'),
        ('Installation2', 'Quality_check_№4', 'Test2', 'Тестировка №2'),
        ('Installation2', 'Fail_quality_check_№4', 'Repair4', 'Ремонт №4'),
        ('Repair4', 'Quality_check_№4.1', 'Test2', 'Тестировка №2'),
        ('Repair4', 'Fail_quality_check_№4.1', 'Repair4', 'Ремонт №4'),
        ('Fail_quality_check_№4', 'Repair4', 'Quality_check_№4.1', 'ОТК №4.1'),
        ('Fail_test', 'Diagnostician_check_repair_№1', 'Repair3', 'Ремонт №3'),
        ('Fail_test2', 'Diagnostician_check_repair_№2', 'Repair5', 'Ремонт №5'),
        ('Fail_test', 'Diagnostician_check_test_№1', 'Test', 'Тестировка'),
        ('Fail_test2', 'Diagnostician_check_test_№2', 'Test2', 'Тестировка №2'),
        ('Quality_check_№4', 'Test2', 'Washing1', 'Отмывка №1'),
        ('Quality_check_№4', 'Fail_test2', 'Diagnostician_check_repair_№2', 'Диагностика №2'),
        ('Quality_check_№4', 'Fail_test2', 'Diagnostician_check_test_№2', 'Диагностика №2'),
        ('Repair5', 'Quality_check_№4.2', 'Test2', 'Тестировка №2'),
        ('Repair5', 'Fail_quality_check_№4.2', 'Repair5', 'Ремонт №5'),
        ('Fail_quality_check_№4.2', 'Repair5', 'Quality_check_№4.2', 'ОТК №4.2'),
        ('Repair6', 'Quality_check_№5.1', 'Washing2', 'Отмывка №2'),
        ('Repair6', 'Fail_quality_check_№5.1', 'Repair6', 'Ремонт №6'),
        ('Test2', 'Washing1', 'Quality_check_№5', 'ОТК №5'),
        ('Diagnostician_check_repair_№1', 'Repair3', 'Quality_check_№3', 'ОТК №3'),
        ('Washing1', 'Quality_check_№5', 'Varnish', 'Нанесение компаунда'),
        ('Washing2', 'Quality_check_№5.1.1', 'Varnish', 'Нанесение компаунда'),
        ('Washing1', 'Fail_quality_check_№5', 'Repair6', 'Ремонт №6'),
        ('Washing2', 'Fail_quality_check_№5.1.1', 'Washing2', 'Отмывка №2'),
        ('Quality_check_№5.1', 'Washing2', 'Quality_check_№5.1.1', 'ОТК №5'),
        ('Fail_quality_check_№5', 'Repair6', 'Quality_check_№5.1', 'ОТК №5.1'),
        ('Quality_check_№5', 'Varnish', 'Quality_check_№6', 'ОТК №6'),
        ('Varnish', 'Quality_check_№6', 'ready', 'Готовые платы'),
        ('Varnish', 'Fail_quality_check_№6', 'Varnish', 'Нанесение компаунда'),
        ('Quality_check_№2', 'Technical', 'Test', 'Тестировка №1'),
        ('Test', 'Technical2', 'Washing1', 'Отмывка №1'),
        ('Side1', 'Technical3', 'Quality_check_№1', 'ОТК №1')
)
INSERT INTO operation_status_routes
    (previous_status_id, current_status_id, next_status_id, next_operation_name)
SELECT previous_status.id, current_status.id, next_status.id, route.operation_name
FROM route_data route
LEFT JOIN operation_statuses previous_status ON previous_status.name = route.previous_name
JOIN operation_statuses current_status ON current_status.name = route.current_name
LEFT JOIN operation_statuses next_status ON next_status.name = route.next_name;
--rollback DELETE FROM operation_status_routes;
